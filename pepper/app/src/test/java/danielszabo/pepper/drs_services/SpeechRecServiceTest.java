package danielszabo.pepper.drs_services;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import fi.oulu.danielszabo.pepper.drs_services.SpeechRecService;
import fi.oulu.danielszabo.pepper.drs_services.SpeechSynthService;
import fi.oulu.danielszabo.pepper.log.LOG;

public class SpeechRecServiceTest {

    private SpeechRecService speechRecService = new SpeechRecService(null);

    @Before
    public void setUp() {
        LOG.setPrintInsteadOfAndroidLog(true);
    }

    @Test
    public void getSpeechRecResponse() {

        byte[] audio = speechRecService.loadAudioFromDisk("./src/test/test_uttering.mp3");
//        speechRecService.recordAudioWithTabletMic(500);
        String result = speechRecService.transcribeAudio(audio);
        LOG.debug(this, "Test Speech Recogniser Response to input 'test_uttering.mp3' : " + result);
        Assert.assertEquals(result, "Hello, this is the test.");

    }

    public static byte[] loadFile() {
       return null;
    }

}
