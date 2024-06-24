package fi.oulu.danielszabo.pepper.services.built_in;


import fi.oulu.danielszabo.pepper.tools.SimpleController;

public class SpeechSynthService implements fi.oulu.danielszabo.pepper.interfaces.SpeechSynthService {
    public void speak(String text) {
        SimpleController.say(text);
    }

}
