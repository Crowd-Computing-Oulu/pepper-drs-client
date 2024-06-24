package fi.oulu.danielszabo.pepper.services.drs;

public class Instruction {
    private String identifier;
    private String[] params;

    public Instruction(String identifier, String... params) {
        this.identifier = identifier;
        this.params = params;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }
}

