package org.example.digitalAdapter.custom.handler;

import java.util.function.Function;

public class DigitalHandler<T> {
    private final Function<T, String> handler;
    private final String topic;
    private final String key;

    public DigitalHandler(String key, String topic, Function<T, String> handler) {
        this.handler = handler;
        this.topic = topic;
        this.key = key;
    }

    public Function<T, String> getHandler() {
        return handler;
    }

    public String getTopic() {
        return topic;
    }

    public String getKey() {
        return key;
    }
}
