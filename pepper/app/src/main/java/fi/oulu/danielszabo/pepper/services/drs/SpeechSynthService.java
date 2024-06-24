package fi.oulu.danielszabo.pepper.services.drs;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.Config;
import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.R;
import fi.oulu.danielszabo.pepper.log.LOG;

public class SpeechSynthService implements fi.oulu.danielszabo.pepper.interfaces.SpeechSynthService {

    public void speak(String text) {
        byte[] audio = obtainAudio(text);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> playAudio(audio));
        if(Config.isSpeech_animations_enabled()) {
            executorService.submit(this::playAnimation);
        }
    }

    public byte[] obtainAudio(String text) {
        LOG.debug(this, "Synthesising speech: '" + text + "'");
        try {
            URL url = new URL(DRSConfig.SPEECH_SYNTH_URL_BASE);

            // Open a connection to the server
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set up the request method and headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // Send the text to the server
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(text);
            writer.flush();
            writer.close();

            // Get the response from the server
            InputStream inputStream = new BufferedInputStream(connection.getInputStream());
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((bytesRead = inputStream.read(buffer, 0, bufferSize)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] audioData = outputStream.toByteArray();
            connection.disconnect();

            LOG.debug(this, "Speech synthesis complete");
            return audioData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void playAudio(byte[] audioData) {
        int audioSampleRate = 22050;
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
        int bufferSize = AudioTrack.getMinBufferSize(audioSampleRate, channelConfig, audioFormat);
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, audioSampleRate, channelConfig, audioFormat, bufferSize, AudioTrack.MODE_STREAM);
        audioTrack.play();
        audioTrack.write(audioData, 0, audioData.length);
        audioTrack.stop();
        audioTrack.release();
    }

    public void playAnimation() {
        // Run the animation only if the TTS request was successful (audioData is not null)
        AnimationBuilder.with(PepperApplication.qiContext)
                .withResources(R.raw.enumeration_right_hand_a001)
                .buildAsync()
                .andThenCompose(animation -> AnimateBuilder.with(PepperApplication.qiContext)
                        .withAnimation(animation)
                        .buildAsync())
                .andThenConsume(Animate::run);
    }
}