package fi.oulu.danielszabo.pepper.services.drs;

import android.os.Handler;
import android.os.Looper;

import com.aldebaran.qi.Consumer;

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
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fi.oulu.danielszabo.pepper.interfaces.InstructionHandler;
import fi.oulu.danielszabo.pepper.log.LOG;

public class ControlService {

    private Timer timer;
    private InstructionHandler instructionHandler;

    public ControlService(InstructionHandler instructionHandler) {
        this.instructionHandler = instructionHandler;

        // Add a log listener for the DRS admin control panel
        LOG.addListener("DRSRemoteLogger", new DRSControlServiceLogListener());

        startFetching();
    }

    public void startFetching() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchInstructions();
            }
        }, 0, 1000); // 1000 milliseconds = 1 second
    }

    public void sendMessage(String message, String... params) {
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
                                "&message=" + URLEncoder.encode(message, "UTF-8") +
                                "&params=" + URLEncoder.encode(String.join(",", params), "UTF-8");

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
                    System.out.println("Response (message): " + response.toString());
                }

                // Close the connection
                connection.disconnect();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void fetchInstructions() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL apiUrl = new URL(DRSConfig.CONTROL_HOST);
            connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("fetch", "fetch");
            payload.put("device", "1");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.toString().getBytes());
            outputStream.flush();

            StringBuilder response = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject jsonResponse = new JSONObject(response.toString());
            String deviceStr = jsonResponse.getString("device");
            String instructionStr = jsonResponse.getString("instruction");
            String paramsStr = jsonResponse.getString("params");

            // Using Handler to post result to UI thread
            new Handler(Looper.getMainLooper()).post(() -> {
                // Assuming handleInstructions is a method of ContentView
                try {
                    instructionHandler.handleInstruction(new Instruction(instructionStr, paramsStr));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException | JSONException e) {
//            e.printStackTrace();
            // Handle exceptions here
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
