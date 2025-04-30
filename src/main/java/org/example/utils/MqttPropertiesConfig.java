package org.example.utils;

public class MqttPropertiesConfig {

    private String host;
    private int port;

    public MqttPropertiesConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static MqttPropertiesConfig getDefault() {
        return new MqttPropertiesConfig("127.0.0.1", 1883);
    }
}
