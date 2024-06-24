package danielszabo.pepper.drs_services;

import java.util.Scanner;

import fi.oulu.danielszabo.pepper.services.drs.ConvAgentService;

// I'm not sure how to actually run this yet...
public class InteractiveConvAgentTest {

    public static void main(String[] args) {
        ConvAgentService ca = new ConvAgentService();
        String userInput = "";
        Scanner scanner = new Scanner(System.in);
        while (!(userInput = scanner.nextLine()).equals("exit")){
            String response = ca.sendUserMessage(userInput);
            System.out.println(response);
        }
    }
}
