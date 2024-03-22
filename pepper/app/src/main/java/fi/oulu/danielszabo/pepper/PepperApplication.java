package fi.oulu.danielszabo.pepper;

import com.aldebaran.qi.sdk.QiContext;

import fi.oulu.danielszabo.pepper.drs_services.ConvAgentService;
import fi.oulu.danielszabo.pepper.drs_services.SpeechRecService;
import fi.oulu.danielszabo.pepper.drs_services.SpeechSynthService;
import fi.oulu.danielszabo.pepper.log.LOG;

public class PepperApplication {

    public static QiContext qiContext;

    public static MainActivity mainActivity;

    //    External Services
    public static ConvAgentService convAgentService;
    public static SpeechRecService speechRecService;
    public static SpeechSynthService synthService;

    public static boolean qiContextInitialised;

    static void initialize(QiContext qiContext, MainActivity mainActivity) {
        LOG.info(PepperApplication.class.getSimpleName(), "Initialising Pepper Application...");

        PepperApplication.qiContext = qiContext;
        PepperApplication.mainActivity = mainActivity;

        if(qiContext != null) {
            qiContextInitialised = true;
            mainActivity.initialised(true);
        } else {
            mainActivity.initialised(false);
        }

    }
}
