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
            return (float) (timeExecution / tso); // percentage (%)
        } else {
            return 0;
        }
    }

    public float M10(String operatingRoomId) {
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds();
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
    public float M11() {
        return this.slotUtilization(UtilizationType.OVER_UTILIZATION);
    }

    /**
     * Method that calculate the M12 kpi "Under Utilization". It retrieves the last patient executed for each slot
     * and calculate the total amount of under utilization.
     * @return
     */
    public float M12() {
        return this.slotUtilization(UtilizationType.UNDER_UTILIZATION);
    }

    /**
     * Calculate the mean turnover time of the day.
     * @return
     */
    public float M13() {
        // mean TT or list of TTs?
        float totalTime = 0;
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds();
        List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
        for(Map.Entry<String, DailySlot> slots: orSlots.entrySet()) {
            DailySlot dailySlots = slots.getValue();
            for(SingleSlot slot: dailySlots.getSlots()) {
                List<Surgery> surgeries = getSurgeriesInSlot(surgeriesExecutedToday, slot);
                for(int i = 0; i < surgeries.size() - 1; i ++) {
                    Surgery surgery1 = surgeries.get(i);
                    Surgery surgery2 = surgeries.get(i + 1);
                    float turnOverTime = getTurnOverTime(surgery1, surgery2);
                    totalTime = totalTime + turnOverTime;
                }
            }
        }
        return totalTime;
    }

    public float M16(int numSurgeriesExecuted, int numSlots) {
        return (float) numSurgeriesExecuted / numSlots;
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
            List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds();
            List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
            double tProgrammed = surgeriesExecutedToday.stream().mapToDouble(Surgery::getEstimatedTime).sum();
            return (float) (tProgrammed / tso);
        } else
            return -1;
    }

    private List<String> getSurgeriesExecutedIds() {
        return this.surgeriesExecuted.stream().map(SurgeryLocation::surgeryId).collect(Collectors.toList());
    }

    private float slotUtilization(UtilizationType type) {
        float totalTime = 0;
        List<String> surgeriesExecutedIds = this.getSurgeriesExecutedIds();
        List<Surgery> surgeriesExecutedToday = allSurgeries.stream().filter(i -> surgeriesExecutedIds.contains(i.getIdSurgery())).toList();
        for(Map.Entry<String, DailySlot> slots: orSlots.entrySet()) {
            DailySlot dailySlots = slots.getValue();
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
