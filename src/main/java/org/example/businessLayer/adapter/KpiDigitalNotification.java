package org.example.businessLayer.adapter;

import java.time.LocalDateTime;

public record KpiDigitalNotification(String id, String type, String desc, float value, LocalDateTime timestamp) {
}
