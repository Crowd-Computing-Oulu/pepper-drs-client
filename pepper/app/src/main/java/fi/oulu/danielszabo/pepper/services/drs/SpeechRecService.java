package fi.oulu.danielszabo.pepper.services.drs;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.util.IOUtils;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SpeechRecService {

    private static final int SAMPLE_RATE = 22_050;


    private Context context;

    private File outputFile;
    private File wavFile;

    private PCMRecorder pcmRecorder;

    public SpeechRecService(Context context) {
        this.context = context;
    }

    private boolean writtenToFile = false;

    public static byte[] loadAudioFromDisk(String fileName) {
       return loadAudioFromDisk(new File(fileName));
    }
    public static byte[] loadAudioFromDisk(File file) {
        byte[] audioBytes = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(audioBytes);
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return audioBytes;
    }


    public void startSpeechRecognition(){
        startRecordingAudioWithTablet();
    }

    public void stopSpeechRecognitionAsync(Consumer<String> callback){
        transcribeAudioAsync(stopRecordingAudioWithTablet(), callback);
    }

    public String stopSpeechRecognition(){
        return transcribeAudio(stopRecordingAudioWithTablet());
    }

    private void startRecordingAudioWithTablet() {
        pcmRecorder = new PCMRecorder();
        try {
            File outputDir = context.getCacheDir();
            outputFile = File.createTempFile("recording", ".pcm", outputDir);
            wavFile = File.createTempFile("recording", ".wav", outputDir);
            pcmRecorder.config(context, String.valueOf(wavFile), SAMPLE_RATE, 2, 16);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pcmRecorder.start();
    }

    private byte[] stopRecordingAudioWithTablet() {
        byte[] audio = loadAudioFromDisk(pcmRecorder.stop());
//        playAudio(audio);
//        new SpeechSynthService().speak("Hey Bitchessss");
        return  audio;
    }

    public void playAudio(byte[] audioData) {
        int audioSampleRate = 18000;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int bufferSize = AudioTrack.getMinBufferSize(audioSampleRate, channelConfig, audioFormat);
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        audioTrack.write(audioData, 0, audioData.length);
        audioTrack.stop();
        audioTrack.release();
    }

    public void transcribeAudioAsync(byte[] audio, Consumer<String> callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                String result = transcribeAudio(audio);
                callback.consume(result);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        });
    }

    public String transcribeAudio(byte[] audio) {
        try {
            URL url = null;
            url = new URL(DRSConfig.SPEECH_REC_URL_BASE);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String boundaryID = UUID.randomUUID().toString();
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundaryID);
            connection.setRequestProperty("Authentication", DRSConfig.SPEECH_REC_API_KEY);
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes("--" + boundaryID + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\""+ outputFile.getName() +"\"\r\n");
            outputStream.writeBytes("Content-Type: audio/pcm\r\n\r\n");
            outputStream.write(audio);
            outputStream.writeBytes("\r\n");
            outputStream.writeBytes("--" + boundaryID + "--\r\n");
            outputStream.flush();
            outputStream.close();

            InputStream inputStream = connection.getInputStream();
            String result = IOUtils.readAllStream(inputStream, "UTF-8");
            inputStream.close();

            if (result != null) {
                JSONObject json = new JSONObject(result);
                String text = json.getString("text").replace("\\n", "").replace("[BLANK_AUDIO]", "").trim();
                return text;
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param input         raw PCM data
     *                      limit of file size for wave file: < 2^(2*4) - 36 bytes (~4GB)
     * @param output        file to encode to in wav format
     * @param channelCount  number of channels: 1 for mono, 2 for stereo, etc.
     * @param sampleRate    sample rate of PCM audio
     * @param bitsPerSample bits per sample, i.e. 16 for PCM16
     * @throws IOException in event of an error between input/output files
     * @see <a href="http://soundfile.sapp.org/doc/WaveFormat/">soundfile.sapp.org/doc/WaveFormat</a>
     */
    static public void PCMToWAV(File input, File output, int channelCount, int sampleRate, int bitsPerSample) throws IOException {
        final int inputSize = (int) input.length();

        try (OutputStream encoded = new FileOutputStream(output)) {
            // WAVE RIFF header
            writeToOutput(encoded, "RIFF"); // chunk id
            writeToOutput(encoded, 36 + inputSize); // chunk size
            writeToOutput(encoded, "WAVE"); // format

            // SUB CHUNK 1 (FORMAT)
            writeToOutput(encoded, "fmt "); // subchunk 1 id
            writeToOutput(encoded, 16); // subchunk 1 size
            writeToOutput(encoded, (short) 1); // audio format (1 = PCM)
            writeToOutput(encoded, (short) channelCount); // number of channelCount
            writeToOutput(encoded, sampleRate); // sample rate
            writeToOutput(encoded, sampleRate * channelCount * bitsPerSample / 8); // byte rate
            writeToOutput(encoded, (short) (channelCount * bitsPerSample / 8)); // block align
            writeToOutput(encoded, (short) bitsPerSample); // bits per sample

            // SUB CHUNK 2 (AUDIO DATA)
            writeToOutput(encoded, "data"); // subchunk 2 id
            writeToOutput(encoded, inputSize); // subchunk 2 size
            copy(new FileInputStream(input), encoded);
        } catch (RuntimeException e) {
            throw e;
        }
    }


    /**
     * Size of buffer used for transfer, by default
     */
    private static final int TRANSFER_BUFFER_SIZE = 10 * 1024;

    /**
     * Writes string in big endian form to an output stream
     *
     * @param output stream
     * @param data   string
     * @throws IOException
     */
    public static void writeToOutput(OutputStream output, String data) throws IOException {
        for (int i = 0; i < data.length(); i++)
            output.write(data.charAt(i));
    }

    public static void writeToOutput(OutputStream output, int data) throws IOException {
        output.write(data >> 0);
        output.write(data >> 8);
        output.write(data >> 16);
        output.write(data >> 24);
    }

    public static void writeToOutput(OutputStream output, short data) throws IOException {
        output.write(data >> 0);
        output.write(data >> 8);
    }

    public static long copy(InputStream source, OutputStream output)
            throws IOException {
        return copy(source, output, TRANSFER_BUFFER_SIZE);
    }

    public static long copy(InputStream source, OutputStream output, int bufferSize) throws IOException {
        long read = 0L;
        byte[] buffer = new byte[bufferSize];
        for (int n; (n = source.read(buffer)) != -1; read += n) {
            output.write(buffer, 0, n);
        }
        return read;
    }
    private String getBufferReadFailureReason(int errorCode) {
        switch (errorCode) {
            case AudioRecord.ERROR_INVALID_OPERATION:
                return "ERROR_INVALID_OPERATION";
            case AudioRecord.ERROR_BAD_VALUE:
                return "ERROR_BAD_VALUE";
            case AudioRecord.ERROR_DEAD_OBJECT:
                return "ERROR_DEAD_OBJECT";
            case AudioRecord.ERROR:
                return "ERROR";
            default:
                return "Unknown (" + errorCode + ")";
        }
    }
}
