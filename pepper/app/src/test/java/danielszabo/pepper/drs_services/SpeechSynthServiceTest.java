package danielszabo.pepper.drs_services;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import fi.oulu.danielszabo.pepper.services.drs.SpeechSynthService;
import fi.oulu.danielszabo.pepper.log.LOG;

public class SpeechSynthServiceTest{

    private SpeechSynthService speechSynthService = new SpeechSynthService();

    @Before
    public void setUp() {
        LOG.setPrintInsteadOfAndroidLog(true);
    }

    @Test
    public void getSpeechSynthResponse() {
        String testString = "Hello! I am Pepper. How are you?";
        byte[] audio = speechSynthService.obtainAudio(testString);
        assert audio.length > 0;
        saveFile(audio);
        LOG.debug(this, "Test TTS Response to input 'Hello! I am Pepper. How are you?' saved to file 'test_output.wav'.");
    }

    public static void saveFile(byte[] audioData) {
        File outputFile = new File("test_output.wav");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(audioData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
