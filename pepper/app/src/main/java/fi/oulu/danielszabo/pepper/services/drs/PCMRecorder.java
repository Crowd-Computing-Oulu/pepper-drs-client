package fi.oulu.danielszabo.pepper.services.drs;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;


class PCMRecorder {
    private final String TAG = "PCMRecorder";
    private int mBufferSize;
    private AudioRecord mRecorder = null;
    private boolean mIsRecording = false;
    private String mTempfilename = null;
    private Thread mRecordingThread = null;
    /* parameters */
    private String mOutputPath;
    private int mSampleRate;
    private int mChannels;
    private int mBitsPerSample;

    private Context context;

    public void config(Context context, String outputPath, int sampleRate, int channels, int bitsPerSample) throws Exception {
        /* reset first before config */
        reset();
        Log.i(TAG, "config outputpath: " + outputPath +
                " sampleRate: " + sampleRate +
                " channels: " + channels +
                " bitsPerSample: " + bitsPerSample);

        mOutputPath = outputPath;
        mSampleRate = sampleRate;
        mChannels = channels;
        mBitsPerSample = bitsPerSample;
        mTempfilename = getTempFilename();

        int audioFormat = (mBitsPerSample == 8) ? AudioFormat.ENCODING_PCM_8BIT : AudioFormat.ENCODING_PCM_16BIT;
        int channelConfig = (mChannels == 1) ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;

        mBufferSize = AudioRecord.getMinBufferSize(mSampleRate, channelConfig, audioFormat);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, mSampleRate,
                channelConfig, audioFormat, mBufferSize);
    }

    public void start() {
        mRecordingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeAudioDataToFile();
            }
        }, "PCMRecorder Thread");

        mRecorder.startRecording();
        mIsRecording = true;
        mRecordingThread.start();
    }

    public File stop() {
        try {
            mIsRecording = false;
            mRecorder.stop();
            return copyWaveFile(mTempfilename, mOutputPath, mSampleRate, mBitsPerSample, mChannels);
        } finally {
            reset();
        }
    }

    public void record(int durationMs) throws Exception {
        start();
        Thread.sleep(durationMs);
        stop();
    }

    public void reset() {
        if (mTempfilename != null) {
            File tmpfile = new File(mTempfilename);
            if (tmpfile.exists()){}
//                tmpfile.delete();
            mTempfilename = null;
        }

        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        mOutputPath = null;
        mSampleRate = 0;
        mChannels = 0;
        mBitsPerSample = 0;
        mRecordingThread = null;
    }

    private String getTempFilename() throws IOException {
        File tmpfile = File.createTempFile("PCMRecorder", "rec");
        return tmpfile.getAbsolutePath();
    }

    private void writeAudioDataToFile() {
        byte data[] = new byte[mBufferSize];
        String filename = mTempfilename;
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int read = 0;
        if(null != os){
            while(mIsRecording){
                read = mRecorder.read(data, 0, mBufferSize);

                if(AudioRecord.ERROR_INVALID_OPERATION != read){
                    try {
                        os.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File copyWaveFile(String inFilename, String outFilename,
                              int sampleRate, int bitsPerSample, int channels) {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = sampleRate;
        long byteRate = bitsPerSample * sampleRate * channels/8;
        byte[] data = new byte[mBufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate, bitsPerSample);

            while(in.read(data) != -1){
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new File(outFilename);
    }

    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate, int bitsPerSample) throws IOException {
        byte[] header = new byte[44];

        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8);  // block align
        header[33] = 0;
        header[34] = (byte) bitsPerSample;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

}