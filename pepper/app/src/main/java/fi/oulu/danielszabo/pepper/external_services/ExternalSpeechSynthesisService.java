package fi.oulu.danielszabo.pepper.external_services;

public class ExternalSpeechSynthesisService implements ExternalService {

    @Override
    public boolean initialise() {
        return false;
    }

    @Override
    public boolean uninitialise() {
        return false;
    }

    @Override
    public Status getStatus() {
        return null;
    }
}
