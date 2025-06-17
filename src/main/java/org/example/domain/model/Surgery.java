package org.example.domain.model;

import java.time.LocalDateTime;

public class Surgery {

    final String idSurgery;
    final LocalDateTime arrivalDate;
    final LocalDateTime programmedDate;
    final LocalDateTime admissionDate;
    final int priority;
    final int estimatedTime;

    public Surgery(String idSurgery, LocalDateTime arrivalDate, LocalDateTime programmedDate, LocalDateTime admissionDate, int priority, int estimatedTime) {
        this.idSurgery = idSurgery;
        this.arrivalDate = arrivalDate;
        this.programmedDate = programmedDate;
        this.admissionDate = admissionDate;
        this.priority = priority;
        this.estimatedTime = estimatedTime;
    }

    public String getIdSurgery() {
        return idSurgery;
    }

    public LocalDateTime getArrivalDate() {
        return arrivalDate;
    }

    public LocalDateTime getProgrammedDate() {
        return programmedDate;
    }

    public LocalDateTime getAdmissionDate() {
        return admissionDate;
    }

    public int getPriority() {
        return priority;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }
}
