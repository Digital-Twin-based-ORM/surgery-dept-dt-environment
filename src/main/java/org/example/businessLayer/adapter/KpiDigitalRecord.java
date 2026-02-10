package org.example.businessLayer.adapter;

import java.time.LocalDateTime;

public record KpiDigitalRecord(String id, String type, String desc, float value, LocalDateTime timestamp) {
}
