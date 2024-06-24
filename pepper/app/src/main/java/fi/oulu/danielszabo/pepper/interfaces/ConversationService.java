package fi.oulu.danielszabo.pepper.interfaces;

import com.aldebaran.qi.Consumer;

public interface ConversationService {

    String sendUserMessage(String message);
    void sendUserMessageAsync(String message, Consumer<String> callback);

    void resetConversation();

    int numMessages();
}
