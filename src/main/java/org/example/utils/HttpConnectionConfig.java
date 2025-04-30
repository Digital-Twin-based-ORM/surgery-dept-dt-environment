package org.example.utils;

public class HttpConnectionConfig {
    private String host;
    private int port;

    public HttpConnectionConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public static HttpConnectionConfig getDefault() {
        return new HttpConnectionConfig("localhost", 8080);
    }

    public static HttpConnectionConfig getWithPort(int port) {
        return new HttpConnectionConfig("localhost", port);
    }
}
