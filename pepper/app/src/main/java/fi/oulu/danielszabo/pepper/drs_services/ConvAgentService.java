package fi.oulu.danielszabo.pepper.drs_services;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import fi.oulu.danielszabo.pepper.log.LOG;

public class ConvAgentService {

    public interface OnMessageSentListener {
        void onMessageSent(JSONObject response, Exception error);
    }

    public String sendUserMessage(String message) {
        final String apiUrl = Config.CA_URL_BASE;
        HttpURLConnection connection = null;
        JSONObject jsonResponse = null;
        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("sender", "robot_user");
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

            String responseStr = response.substring(1, response.length()-1);

            LOG.debug(this, "CA message exchange: " + message + " - " + response);

            // cut off trailing and ending brackets []
            jsonResponse = new JSONObject(responseStr);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        try {
            return jsonResponse.get("text").toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void sendUserMessageAsync(String message, Consumer<String> callback) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            callback.accept(sendUserMessage(message));
        });
    }

}
