package fi.oulu.danielszabo.pepper.services.drs;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.log.LogEntry;
import fi.oulu.danielszabo.pepper.log.LogListener;

public class DRSControlServiceLogListener implements LogListener {
    @Override
    public void accept(LogEntry logEntry) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            try {


                // Define the URL and create a connection
                URL apiUrl = new URL(DRSConfig.CONTROL_HOST);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();

                // Set the request method and headers
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setDoOutput(true);

                // Create the POST data
                String postData =
                        "log=log" +
                                "&device=0" +
                                "&type=" + URLEncoder.encode(logEntry.getLogLevel().toString(), "UTF-8") +
                                "&content=" + URLEncoder.encode(logEntry.getTag() + ": " + logEntry.getMessage(), "UTF-8");

                // Write the POST data to the connection
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(postData.getBytes("UTF-8"));
                }

                // Get the response
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    StringBuilder response = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    System.out.println("Response: " + response.toString());
                }

                // Close the connection
                connection.disconnect();
            } catch (ProtocolException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}