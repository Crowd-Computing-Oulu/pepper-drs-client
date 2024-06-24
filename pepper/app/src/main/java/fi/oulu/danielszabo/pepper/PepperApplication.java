package fi.oulu.danielszabo.pepper;

import android.os.StrictMode;

import com.aldebaran.qi.sdk.QiContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.core.ConvAgentInstructionHandler;
import fi.oulu.danielszabo.pepper.interfaces.InstructionHandler;
import fi.oulu.danielszabo.pepper.screens.main.MainFragment;
import fi.oulu.danielszabo.pepper.services.chatgpt.ChatGPTConfig;
import fi.oulu.danielszabo.pepper.services.chatgpt.ChatGPTConvService;
import fi.oulu.danielszabo.pepper.services.drs.ControlService;
import fi.oulu.danielszabo.pepper.services.drs.ConvAgentService;
import fi.oulu.danielszabo.pepper.services.drs.DRSControlServiceInstructionHandler;
import fi.oulu.danielszabo.pepper.services.drs.SpeechRecService;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.interfaces.ConversationService;
import fi.oulu.danielszabo.pepper.interfaces.SpeechSynthService;

public class PepperApplication {

//    just for dev purposes
    public static final boolean MOCK_SPEECH_INPUT = false;
    public static QiContext qiContext;

    public static MainActivity mainActivity;


    //    External Services
    public static ConvAgentService convAgentService;
    public static SpeechRecService speechRecService;
    private static SpeechSynthService nativeSpeechSyncService, externalSpeechSyncService;
    private static  ChatGPTConvService chatGPTConvService;
    private static ControlService drsControlService;
    public static boolean qiContextInitialised;
    private static ConvAgentInstructionHandler convAgentInstructionHandler;
    private static DRSControlServiceInstructionHandler drsControLServiceInstructionHandler;

    static void initialize(QiContext qiContext, MainActivity mainActivity) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            LOG.info(PepperApplication.class.getSimpleName(), "Initialising Pepper Application...");

            PepperApplication.qiContext = qiContext;
            PepperApplication.mainActivity = mainActivity;

            convAgentService = new ConvAgentService();
            chatGPTConvService = new ChatGPTConvService(ChatGPTConfig.API_TOKEN);
            speechRecService = new SpeechRecService(mainActivity);
            drsControLServiceInstructionHandler = new DRSControlServiceInstructionHandler();
            drsControlService = new ControlService(drsControLServiceInstructionHandler);
            convAgentInstructionHandler = new ConvAgentInstructionHandler();

            nativeSpeechSyncService = new fi.oulu.danielszabo.pepper.services.built_in.SpeechSynthService();
            externalSpeechSyncService = new fi.oulu.danielszabo.pepper.services.drs.SpeechSynthService();

            if(qiContext != null) {
                qiContextInitialised = true;
                mainActivity.initialised(true);
            } else {
                mainActivity.initialised(false);
            }
        });
    }

    public static SpeechSynthService getSpeechSynthService(){
        if(Config.isMimic3_enabled()){
            return externalSpeechSyncService;
        } else {
            return nativeSpeechSyncService;
        }
    }

    public static ConversationService getConversationService(){
        if(Config.isChatGPT_enabled()){
            return chatGPTConvService;
        } else {
            return convAgentService;
        }
    }

    public static ControlService getControlService(){
        return drsControlService;
    }

    public static InstructionHandler getConvAgentInstructionHandler() {
        return convAgentInstructionHandler;
    }

    public static InstructionHandler getControlServiceInstructionHandler() {
        return drsControLServiceInstructionHandler;
    }

}
