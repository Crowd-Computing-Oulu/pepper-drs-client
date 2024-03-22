package fi.oulu.danielszabo.pepper.drs_services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.sdk.util.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.log.LOG;

public class SpeechRecService {

    private static final int SAMPLE_RATE = 44100;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord audioRecord;
    private boolean isRecording = false;


    private Context context;

    public SpeechRecService(Context context) {
        this.context = context;
    }

    public static byte[] loadAudioFromDisk(String fileName) {
        File file = new File(fileName);
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
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);

        if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
            isRecording = true;
            audioRecord.startRecording();
            LOG.debug(this, "Recording started");
        } else {
            LOG.debug(this, "Failed to initialize AudioRecord");
        }
    }

    private byte[] stopRecordingAudioWithTablet() {
        if (isRecording) {
            isRecording = false;
            audioRecord.stop();
            audioRecord.release();
            LOG.debug(this, "Recording stopped");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                FileInputStream inputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.pcm");
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
            } catch (IOException e) {
                LOG.debug(this, "Error reading recording file: " + e.toString());
            }
            return outputStream.toByteArray();
        } else {
            LOG.debug(this, "Recording not started");
            return null;
        }
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
            url = new URL(Config.SPEECH_REC_URL_BASE);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            String boundaryID = UUID.randomUUID().toString();
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundaryID);
            connection.setRequestProperty("Authentication", Config.SPEECH_REC_API_KEY);
            connection.setDoOutput(true);

            DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
            outputStream.writeBytes("--" + boundaryID + "\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"recording.wav\"\r\n");
            outputStream.writeBytes("Content-Type: audio/wav\r\n\r\n");
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
}
