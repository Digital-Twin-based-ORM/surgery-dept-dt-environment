package org.example.utils;

import org.example.domain.model.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class KpiCalculator {

    private enum UtilizationType {
        UNDER_UTILIZATION, OVER_UTILIZATION
    }

    ArrayList<Surgery> allSurgeries;
    ArrayList<SurgeryLocation> surgeriesExecuted;
    Map<String, DailySlot> orSlots;

    public KpiCalculator(ArrayList<Surgery> allSurgeries, ArrayList<SurgeryLocation> surgeriesExecuted, Map<String, DailySlot> orSlots) {
        this.allSurgeries = allSurgeries;
        this.surgeriesExecuted = surgeriesExecuted;
        this.orSlots = orSlots;
    }

    /**
     * Method that calculate the M0 kpi "Raw Utilization". It calculate the overall utilization of a specific
     * operating room having in mind its total operating time assigned.
     * @param idOperatingRoom
     * @return
     */
    public float M9(String idOperatingRoom) {
        float timeExecution = 0;
        if(orSlots.containsKey(idOperatingRoom)) {
            DailySlot dailySlot = orSlots.get(idOperatingRoom);
            double tso = getTSO(dailySlot);
            System.out.println("TSO sala " + idOperatingRoom + ": " + tso);
            List<String> surgeriesID = surgeriesExecuted.stream().filter(i -> Objects.equals(i.operationRoomId(), idOperatingRoom)).map(SurgeryLocation::surgeryId).toList();
            List<Surgery> surgeries = allSurgeries.stream().filter(i -> surgeriesID.contains(i.getIdSurgery())).toList();
            for(Surgery surgery : surgeries) {
                String outSO = surgery.getEventTimestamp(SurgeryEvents.OutSO);
                String inSO = surgery.getEventTimestamp(SurgeryEvents.InSO);
                if(!Objects.equals(outSO, "") && !Objects.equals(inSO, "")) {
                    LocalDateTime outSODateTime = LocalDateTime.parse(outSO);
                    LocalDateTime inSODateTime = LocalDateTime.parse(inSO);
                    timeExecution = timeExecution + (Duration.between(inSODateTime, outSODateTime).toMinutes());
                }
            }
            System.out.println("Time execution sala " + idOperatingRoom + ": " + timeExecution);
            return (float) (timeExecution / tso); // percentage (%)
        } else {
            return 0;
        }
    }

    public float M10(String operatingRoomId) {
        // start time tardiness per ogni sala operatoria: indica il ritardo del primo intervento chirurgico della giornata nella specifica sala operatoria
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds(operatingRoomId);
        List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
        DailySlot slots = orSlots.get(operatingRoomId);
        SingleSlot slot = slots.getSlots().getFirst();
        List<Surgery> surgeries = orderByProgrammedTime(getSurgeriesInSlot(surgeriesExecutedToday, slot));
        Surgery firstSurgeryOfTheDay = surgeries.getFirst();
        LocalTime stCh = LocalDateTime.parse(firstSurgeryOfTheDay.getEventTimestamp(SurgeryEvents.StCh)).toLocalTime();
        LocalTime programmedDate = firstSurgeryOfTheDay.getProgrammedDate().toLocalTime();
        System.out.println("STCH: " + stCh + " - programmed: " + programmedDate);
        return Duration.between(programmedDate, stCh).toMinutes();
    }

    /**
     * Total over time of the day.
     * @return
     */
    public float M11(String operatingRoom) {
        return this.slotUtilization(UtilizationType.OVER_UTILIZATION, operatingRoom);
    }

    /**
     * Method that calculate the M12 kpi "Under Utilization". It retrieves the last patient executed for each slot
     * and calculate the total amount of under utilization.
     * @return
     */
    public float M12(String operatingRoom) {
        return this.slotUtilization(UtilizationType.UNDER_UTILIZATION, operatingRoom);
    }

    /**
     * Calculate the mean turnover time of the day.
     * @return
     */
    public float M13(String idOperatingRoom) {
        // TODO Total turn over time for a specified operating room??
        float totalTime = 0;
        float totalSurgeries = 0;
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds(idOperatingRoom);
        List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();

        DailySlot slots = orSlots.get(idOperatingRoom);
        for(SingleSlot slot: slots.getSlots()) {
            List<Surgery> surgeries = getSurgeriesInSlot(surgeriesExecutedToday, slot);
            System.out.println(Arrays.toString(surgeries.toArray()));
            for(int i = 0; i < surgeries.size() - 1; i ++) {
                Surgery surgery1 = surgeries.get(i);
                Surgery surgery2 = surgeries.get(i + 1);
                float turnOverTime = getTurnOverTime(surgery1, surgery2);
                totalTime = totalTime + turnOverTime;
            }
            totalSurgeries = totalSurgeries + surgeries.size();
        }

        return totalTime / totalSurgeries;
    }

    public float M16(String idOperatingRoom) {
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds(idOperatingRoom);
        int numSlots = orSlots.get(idOperatingRoom).slots.size();
        return (float) surgeriesExecutedIds.size() / numSlots;
    }

    public float M18() {
        long surgeriesCancelled = allSurgeries.stream().filter(Surgery::isCancelled).count();
        long numSurgeries = allSurgeries.stream().filter(Surgery::isProgrammed).count();
        return (float) surgeriesCancelled / numSurgeries;
    }

    public float M21(String idOperatingRoom) {
        if(orSlots.containsKey(idOperatingRoom)) {
            DailySlot dailySlot = orSlots.get(idOperatingRoom);
            double tso = getTSO(dailySlot);
            List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds(idOperatingRoom);
            List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
            double tProgrammed = surgeriesExecutedToday.stream().mapToDouble(Surgery::getEstimatedTime).sum();
            return (float) (tProgrammed / tso);
        } else
            return -1;
    }

    public float M22() {
        // Total turn over time for a specified operating room
        float totalTTProlonged = 0;
        int totalTurnOverTime = 0;
        for(Map.Entry<String, DailySlot> operatingRoom : orSlots.entrySet()) {
            List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds(operatingRoom.getKey());
            List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
            DailySlot slots = operatingRoom.getValue();
            for(SingleSlot slot: slots.getSlots()) {
                List<Surgery> surgeries = getSurgeriesInSlot(surgeriesExecutedToday, slot);
                System.out.println(Arrays.toString(surgeries.toArray()));
                for(int i = 0; i < surgeries.size() - 1; i ++) {
                    Surgery surgery1 = surgeries.get(i);
                    Surgery surgery2 = surgeries.get(i + 1);
                    float turnOverTime = getTurnOverTime(surgery1, surgery2);
                    totalTurnOverTime += 1;
                    if(turnOverTime > 60) {
                        totalTTProlonged += 1;
                    }
                }
            }
        }
        if(totalTurnOverTime == 0) {
            return -1;
        }
        return totalTTProlonged / totalTurnOverTime;
    }

    public float M24() {
        int totElectiveSessions = surgeriesExecuted.size();
        int totUrgentSessions = 0;
        List<String> surgeriesExecutedIds = this.surgeriesExecuted.stream().map(SurgeryLocation::surgeryId).toList();
        List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
        for(Surgery surgeryExecuted : surgeriesExecutedToday) {
            if(surgeryExecuted.getHospitalizationRegime().equals(HospitalizationRegime.URGENT)) {
                totUrgentSessions += 1;
            }
        }
        return (float) totUrgentSessions / totElectiveSessions;
    }

    private List<String> getSurgeriesExecutedIds(String idOperatingRoom) {
        return this.surgeriesExecuted.stream().filter(i -> Objects.equals(i.operationRoomId(), idOperatingRoom)).map(SurgeryLocation::surgeryId).toList();
    }

    private float slotUtilization(UtilizationType type, String operatingRoom) {
        float totalTime = 0;
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds(operatingRoom);
        List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
        DailySlot dailySlots = orSlots.get(operatingRoom);
        for(SingleSlot slot: dailySlots.getSlots()) {
            List<Surgery> surgeries = getSurgeriesInSlot(surgeriesExecutedToday, slot);
            ArrayList<LocalTime> localTimes = new ArrayList<>(surgeries.stream()
                    .map(i -> i.getEventTimestamp(SurgeryEvents.OutSO))
                    .map(LocalDateTime::parse)
                    .map(LocalDateTime::toLocalTime)
                    .sorted()
                    .toList());
            LocalTime lastSurgery = localTimes.getLast();
            switch (type) {
                case OVER_UTILIZATION -> totalTime = totalTime + slot.getOverTime(lastSurgery);
                case UNDER_UTILIZATION -> totalTime = totalTime + slot.getUnderUtilizationTime(lastSurgery);
            }
        }
        return totalTime;
    }

    public float getTurnOverTime(Surgery surgery1, Surgery surgery2) {
        LocalTime inTime = LocalDateTime.parse(surgery2.getEventTimestamp(SurgeryEvents.InSO)).toLocalTime();
        LocalTime outTime = LocalDateTime.parse(surgery1.getEventTimestamp(SurgeryEvents.OutSO)).toLocalTime();
        return Duration.between(outTime, inTime).toMinutes();
    }

    private List<Surgery> getSurgeriesInSlot(List<Surgery> surgeriesExecutedToday, SingleSlot slot) {
        return surgeriesExecutedToday.stream().filter(i -> {
            LocalDateTime programmedDate = i.getProgrammedDate();
            return slot.isSurgeryProgrammed(programmedDate.toLocalTime());
        }).toList();
    }

    public List<Surgery> orderByProgrammedTime(List<Surgery> surgeries) {
        return surgeries.stream().sorted(Comparator.comparing(Surgery::getProgrammedDate)).collect(Collectors.toList());
    }

    public double getTSO(DailySlot dailySlot) {
        return dailySlot.getSlots().stream().mapToDouble(SingleSlot::getMinutesDuration).sum();
    }
}
