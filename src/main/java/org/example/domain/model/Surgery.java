package org.example.domain.model;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Surgery {

    private String idSurgery;
    private LocalDateTime arrivalDate;
    private LocalDateTime programmedDate;
    private LocalDateTime admissionDate;
    private PriorityClass priority;
    private int estimatedTime;
    private HospitalizationRegime hospitalizationRegime;
    private Map<SurgeryEvents, Long> eventsTimestamps = new HashMap<>();

    public Surgery(String idSurgery, PriorityClass priority) {
        this.idSurgery = idSurgery;
        this.priority = priority;
    }

    public Surgery(String idSurgery, LocalDateTime arrivalDate, LocalDateTime programmedDate, LocalDateTime admissionDate, LocalDateTime executedDate, PriorityClass priority, int estimatedTime) {
        this.idSurgery = idSurgery;
        this.arrivalDate = arrivalDate;
        this.programmedDate = programmedDate;
        this.admissionDate = admissionDate;
        this.priority = priority;
        this.estimatedTime = estimatedTime;
    }

    public Surgery(String idSurgery, LocalDateTime programmedDate, LocalDateTime admissionDate, PriorityClass priority, HospitalizationRegime hospitalizationRegime) {
        this.idSurgery = idSurgery;
        this.arrivalDate = admissionDate.minusMinutes(10);
        this.programmedDate = programmedDate;
        this.admissionDate = admissionDate;
        this.priority = priority;
        this.hospitalizationRegime = hospitalizationRegime;
    }

    public void addTimestamp(SurgeryEvents event, Long timestamp) {
        this.eventsTimestamps.put(event, timestamp);
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

    public PriorityClass getPriority() {
        return priority;
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public void setIdSurgery(String idSurgery) {
        this.idSurgery = idSurgery;
    }

    public void setArrivalDate(LocalDateTime arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public void setProgrammedDate(LocalDateTime programmedDate) {
        this.programmedDate = programmedDate;
    }

    public void setAdmissionDate(LocalDateTime admissionDate) {
        this.admissionDate = admissionDate;
    }
    public void setPriority(PriorityClass priority) {
        this.priority = priority;
    }

    public void setEstimatedTime(int estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setEventsTimestamps(Map<SurgeryEvents, Long> eventsTimestamps) {
        this.eventsTimestamps = eventsTimestamps;
    }

    public HospitalizationRegime getHospitalizationRegime() {
        return hospitalizationRegime;
    }

    public void setHospitalizationRegime(HospitalizationRegime hospitalizationRegime) {
        this.hospitalizationRegime = hospitalizationRegime;
    }
}
