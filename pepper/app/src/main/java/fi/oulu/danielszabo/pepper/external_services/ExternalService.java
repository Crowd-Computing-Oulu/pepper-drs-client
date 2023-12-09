package fi.oulu.danielszabo.pepper.external_services;

public interface ExternalService {

    enum Status {
        CONNECTED,
        CONNECTING,
        DISCONNECTED,
        FAILED;
    }

    boolean initialise();

    boolean uninitialise();

    Status getStatus();
}
