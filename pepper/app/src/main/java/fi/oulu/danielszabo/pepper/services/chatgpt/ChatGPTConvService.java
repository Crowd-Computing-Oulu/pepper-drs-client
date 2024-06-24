package fi.oulu.danielszabo.pepper.services.chatgpt;


import com.aldebaran.qi.Consumer;
import com.theokanning.openai.service.OpenAiService;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.interfaces.ConversationService;

public class ChatGPTConvService implements ConversationService {

    private static final String MODEL = "gpt-3.5-turbo";
    private static final int MAX_TOKENS = 1500;
    private static final double RESPONSE_TEMPERATURE = 1.0; // 0.2 -> relatively consistent, 0.7 -> a bit more creative, 1.0 -> madness
    private static final double FREQUENCY_PENALTY = 0.0;
    private static final double PRESENCE_PENALTY = 0.6;
    private static final int USER_OPTION_NUMBER = 5;

    private final OpenAiService openAiService;

    private Conversation conversation;

    public ChatGPTConvService(String apiToken) {
        this.openAiService = new OpenAiService(apiToken);
    }

    public Conversation startConversation() {
        return conversation = 
                new Conversation(openAiService, MODEL, RESPONSE_TEMPERATURE,
                        FREQUENCY_PENALTY,
                        PRESENCE_PENALTY, MAX_TOKENS, USER_OPTION_NUMBER);
    }

    public String sendUserMessage(String input) {
        return conversation.respondTo(input);
    }

    public void sendUserMessageAsync(String input, Consumer<String> callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                callback.consume(conversation.respondTo(input));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void resetConversation() {
        startConversation();
    }

    @Override
    public int numMessages() {
        if(conversation == null) return 0;
        return conversation.length();
    }


}