package danielszabo.pepper.drs_services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import fi.oulu.danielszabo.pepper.services.drs.ConvAgentService;
import fi.oulu.danielszabo.pepper.log.LOG;

public class ConvAgentServiceTest {

    private ConvAgentService convAgentService = new ConvAgentService();

    @Before
    public void setUp() {
        LOG.setPrintInsteadOfAndroidLog(true);
    }

    @Test
    public void getCAResponse() {

        String response = convAgentService.sendUserMessage("can you hop into my watch");

        LOG.debug(this, "Test CA Response to input 'test_message' : " + response);

        assertNotNull(response);
        assertTrue(response.length() > 0);
    }

}
