package org.example.domain.model;

public class Warning {

    private Long timestamp;
    private String description;

    public Warning(Long timestamp, String description) {
        this.timestamp = timestamp;
        this.description = description;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }
}
