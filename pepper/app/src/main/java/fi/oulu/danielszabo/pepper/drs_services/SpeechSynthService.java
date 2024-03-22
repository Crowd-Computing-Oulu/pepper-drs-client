package fi.oulu.danielszabo.pepper.drs_services;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.util.Log;

import com.aldebaran.qi.sdk.builder.AnimateBuilder;
import com.aldebaran.qi.sdk.builder.AnimationBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.actuation.Animate;
import com.aldebaran.qi.sdk.object.conversation.Say;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.R;

public class SpeechSynthService {

    public void speak(String text) {
        byte[] audio = obtainAudio(text);
        playAudio(audio);
        playAnimation();
    }

    public byte[] obtainAudio(String text) {
        try {
            URL url = new URL(Config.SPEECH_SYNTH_URL_BASE);

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

            return audioData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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