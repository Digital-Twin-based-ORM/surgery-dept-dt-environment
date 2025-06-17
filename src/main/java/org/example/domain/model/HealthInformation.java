package org.example.domain.model;

import java.util.*;
import java.util.stream.Collectors;

public class HealthInformation {

    private final HashMap<Long, Integer> heartRateValues = new HashMap<>();

    public HealthInformation() {
    }

    public synchronized void addRegisteredBPM(Long timestamp, Integer value) {
        heartRateValues.put(timestamp, value);
    }

    public synchronized List<Integer> getLastValues() {
        return heartRateValues.entrySet().stream().sorted(Map.Entry.comparingByKey(Collections.reverseOrder())).limit(10).map(Map.Entry::getValue).collect(Collectors.toList());
    }
}
