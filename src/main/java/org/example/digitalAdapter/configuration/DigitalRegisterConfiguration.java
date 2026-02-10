package org.example.digitalAdapter.configuration;

public class DigitalRegisterConfiguration {

    private final String url;
    private final String port;
    private final String type;
    private final String name;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getPort() {
        return port;
    }

    public DigitalRegisterConfiguration(String url, String port, String type, String name) {
        this.url = url;
        this.port = port;
        this.type = type;
        this.name = name;
    }

}
