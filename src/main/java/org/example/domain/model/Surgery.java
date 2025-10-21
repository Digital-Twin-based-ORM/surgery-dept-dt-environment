package org.example.domain.model;

import java.time.Duration;
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
    private Map<SurgeryEvents, String> eventsTimestamps = new HashMap<>();
    private Map<String, Float> surgeryKpi = new HashMap<>();
    private LocalDateTime recoveryDate;
    private LocalDateTime waitingListInsertionDate;
    private boolean isCancelled = false;

    public Surgery(String idSurgery, PriorityClass priority) {
        this.idSurgery = idSurgery;
        this.priority = priority;
    }
// TODO do with builder? too much constructors?
    public Surgery(String idSurgery, LocalDateTime arrivalDate, LocalDateTime programmedDate, LocalDateTime admissionDate, LocalDateTime executedDate, PriorityClass priority, int estimatedTime) {
        this.idSurgery = idSurgery;
        this.arrivalDate = arrivalDate;
        this.programmedDate = programmedDate;
        this.admissionDate = admissionDate;
        this.priority = priority;
        this.estimatedTime = estimatedTime;
    }

    public Surgery(String idSurgery, LocalDateTime programmedDate, LocalDateTime admissionDate, PriorityClass priority, HospitalizationRegime hospitalizationRegime, int estimatedTime) {
        this.idSurgery = idSurgery;
        this.arrivalDate = admissionDate.minusMinutes(10);
        this.programmedDate = programmedDate;
        this.admissionDate = admissionDate;
        this.priority = priority;
        this.hospitalizationRegime = hospitalizationRegime;
        this.estimatedTime = estimatedTime;
    }

    public void addTimestamp(SurgeryEvents event, String timestamp) {
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
    public void setEventsTimestamps(Map<SurgeryEvents, String> eventsTimestamps) {
        this.eventsTimestamps = eventsTimestamps;
    }
    public String getEventTimestamp(SurgeryEvents event) {
        return this.eventsTimestamps.getOrDefault(event, "");
    }
    public HospitalizationRegime getHospitalizationRegime() {
        return hospitalizationRegime;
    }
    public void setHospitalizationRegime(HospitalizationRegime hospitalizationRegime) {
        this.hospitalizationRegime = hospitalizationRegime;
    }
    public void setKpi(String key, float value) {
        this.surgeryKpi.put(key, value);
    }
    public float getKpi(String key) {
        return this.surgeryKpi.get(key);
    }
    public boolean hasKpiSet(String key) { return this.surgeryKpi.containsKey(key); }
    public void setRecoveryDate(LocalDateTime recoveryDate) {
        this.recoveryDate = recoveryDate;
    }
    public float waitingTime() {
        // M2
        return Duration.between(waitingListInsertionDate, recoveryDate).toDays();
    }
    /**
     * Only if the patient has not been recovered yet.
     * @return the days passed from the insertion in waiting list and compared to the maximum time wait of its priority class.
     */
    public boolean isOverThreshold() {
        // M3
        if(recoveryDate == null) {
            float duration = Duration.between(waitingListInsertionDate, LocalDateTime.now()).toDays();
            return duration > priority.maxTime;
        } else {
            return false;
        }
    }
    public void cancelSurgery(){
        this.isCancelled = true;
    }
    public boolean isCancelled() {
        return isCancelled;
    }
    public boolean isProgrammed() {
        return programmedDate != null;
    }
}
