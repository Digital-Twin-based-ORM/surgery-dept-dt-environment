package org.example.digitalAdapter.configuration;

import java.util.List;

public class VSMConfiguration {
    private final List<String> observedProperties;

    public VSMConfiguration(List<String> observedProperties) {
        this.observedProperties = observedProperties;
    }

    public List<String> getObservedProperties() {
        return observedProperties;
    }
}
