package fi.oulu.danielszabo.pepper;

import com.aldebaran.qi.sdk.QiContext;

import fi.oulu.danielszabo.pepper.external_services.ExternalControlService;
import fi.oulu.danielszabo.pepper.external_services.ExternalConversationService;
import fi.oulu.danielszabo.pepper.external_services.ExternalSpeechRecognitionService;
import fi.oulu.danielszabo.pepper.external_services.ExternalSpeechSynthesisService;
import fi.oulu.danielszabo.pepper.log.LOG;

public class PepperApplication {

    public static QiContext qiContext;

    public static MainActivity mainActivity;

    //    External Services
    public static ExternalControlService externalControlService;
    public static ExternalConversationService externalConversationService;
    public static ExternalSpeechRecognitionService externalSpeechRecognitionService;
    public static ExternalSpeechSynthesisService externalSpeechSynthesisService;

    public static boolean qiContextInitialised;

    static void initialize(QiContext qiContext, MainActivity mainActivity) {
        LOG.info(PepperApplication.class.getSimpleName(), "Initialising Pepper Application...");

        PepperApplication.qiContext = qiContext;
        PepperApplication.mainActivity = mainActivity;

        LOG.info(PepperApplication.class.getSimpleName(), "Initialising external control service...");
        externalControlService = new ExternalControlService();
        if(!externalControlService.initialise()){
            mainActivity.initialised(false);
        }

        LOG.info(PepperApplication.class.getSimpleName(), "Initialising external conversation service...");
        externalConversationService = new ExternalConversationService();
        if(!externalConversationService.initialise()){
            mainActivity.initialised(false);
        }

        LOG.info(PepperApplication.class.getSimpleName(), "Initialising external speech recognition service...");
        externalSpeechRecognitionService = new ExternalSpeechRecognitionService();
        if(!externalSpeechRecognitionService.initialise()){
            mainActivity.initialised(false);
        }

        LOG.info(PepperApplication.class.getSimpleName(), "Initialising external speech synthesis service...");
        externalSpeechSynthesisService = new ExternalSpeechSynthesisService();
        if(!externalSpeechSynthesisService.initialise()){
            mainActivity.initialised(false);
        }

        if(qiContext != null) {
            qiContextInitialised = true;
            mainActivity.initialised(true);
        } else {
            mainActivity.initialised(false);
        }

    }
}
