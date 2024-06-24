package fi.oulu.danielszabo.pepper.services.drs;

import com.aldebaran.qi.Consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.PepperApplication;
import fi.oulu.danielszabo.pepper.core.ConvAgentInstructionHandler;
import fi.oulu.danielszabo.pepper.log.LOG;
import fi.oulu.danielszabo.pepper.interfaces.ConversationService;

public class ConvAgentService  implements ConversationService {

    List<String> messages = new ArrayList<>();

    public interface OnMessageSentListener {
        void onMessageSent(JSONObject response, Exception error);
    }

    public String sendUserMessage(String message) {
        messages.add(message);
        try {
            final String apiUrl = DRSConfig.CA_URL_BASE;
            HttpURLConnection connection = null;
            JSONArray jsonResponse = null;
            try {
                URL url = new URL(apiUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                JSONObject payload = new JSONObject();
                payload.put("sender", "user1");
                payload.put("message", message);

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(payload.toString().getBytes());
                outputStream.flush();
                outputStream.close();

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();


                LOG.debug(this, "CA message exchange: " + message + " - " + response);

                // cut off trailing and ending brackets []
                jsonResponse = new JSONArray(response.toString());


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
            if (jsonResponse.length() > 1 &&  jsonResponse.getJSONObject(1).has("custom")) {
                if (jsonResponse.getJSONObject(1).getJSONObject("custom").has("device_instruction")) {
                    PepperApplication.getConvAgentInstructionHandler().handleInstruction(new Instruction(jsonResponse.getJSONObject(1).getJSONObject("custom").get("device_instruction").toString()));
                }
            }
            String res = jsonResponse.getJSONObject(0).get("text").toString();
            messages.add(res);
            return res;
        } catch (JSONException e) {
            throw new RuntimeException();
        }

    }

    public void sendUserMessageAsync(String message, Consumer<String> callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {
                callback.consume(sendUserMessage(message));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void resetConversation() {
//        none
        messages.clear();
    }

    @Override
    public int numMessages() {
        return messages.size();
    }

}
