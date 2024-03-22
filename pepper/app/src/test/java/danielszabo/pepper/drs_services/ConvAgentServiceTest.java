package danielszabo.pepper.drs_services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;

import fi.oulu.danielszabo.pepper.drs_services.ConvAgentService;
import fi.oulu.danielszabo.pepper.log.LOG;

public class ConvAgentServiceTest {

    private ConvAgentService convAgentService = new ConvAgentService();

    @Before
    public void setUp() {
        LOG.setPrintInsteadOfAndroidLog(true);
    }

    @Test
    public void getCAResponse() {

        String response = convAgentService.sendUserMessage("test_message");

        LOG.debug(this, "Test CA Response to input 'test_message' : " + response);

        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

}
