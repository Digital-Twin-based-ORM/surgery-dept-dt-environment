package org.example;

import org.example.domain.model.*;
import org.example.utils.KpiCalculator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class KpiCalculatorScenario2Test {
    ArrayList<Surgery> allSurgeries = new ArrayList<>();
    ArrayList<SurgeryLocation> surgeriesExecuted = new ArrayList<>();
    Map<String, DailySlot> orSlots = new HashMap<>();
    KpiCalculator kpiCalculator;

    @BeforeEach
    void setUp() {
        /** SURGERY 1 **/
        Surgery surgery1_1 = new Surgery("1", PriorityClass.A);
        LocalDateTime programmed1_1 = LocalDateTime.of(LocalDate.now(), LocalTime.parse("09:30:00"));
        LocalDateTime startTime1_1 = LocalDateTime.of(LocalDate.now(), LocalTime.parse("09:30:00"));
        surgery1_1.setProgrammedDate(programmed1_1);
        surgery1_1.setEstimatedTime(60);
        surgery1_1.addTimestamp(SurgeryEvents.StCh, startTime1_1.toString());
        surgery1_1.addTimestamp(SurgeryEvents.InSO, startTime1_1.toString());
        surgery1_1.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("10:30:00")).toString());

        /** SURGERY 2 **/
        Surgery surgery1_2 = new Surgery("2", PriorityClass.A);
        LocalDateTime programmed1_2 = LocalDateTime.of(LocalDate.now(), LocalTime.parse("11:00:00"));
        surgery1_2.setProgrammedDate(programmed1_2);
        surgery1_2.setEstimatedTime(45);
        surgery1_2.addTimestamp(SurgeryEvents.StCh, programmed1_2.toString());
        surgery1_2.addTimestamp(SurgeryEvents.InSO, programmed1_2.toString());
        surgery1_2.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("11:45:00")).toString());

        /** SURGERY 3 **/
        Surgery surgery2_1 = new Surgery("3", PriorityClass.A);
        LocalDateTime programmed2_1 = LocalDateTime.of(LocalDate.now(), LocalTime.parse("14:15:00"));
        surgery2_1.setProgrammedDate(programmed2_1);
        surgery2_1.setEstimatedTime(45);
        surgery2_1.addTimestamp(SurgeryEvents.InSO, programmed2_1.toString());
        surgery2_1.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("15:00:00")).toString());

        /** SURGERY 4 **/
        Surgery surgery2_2 = new Surgery("4", PriorityClass.A);
        LocalDateTime programmed2_2 = LocalDateTime.of(LocalDate.now(), LocalTime.parse("15:30:00"));
        surgery2_2.setProgrammedDate(programmed2_2);
        surgery2_2.setEstimatedTime(50);
        surgery2_2.addTimestamp(SurgeryEvents.InSO, programmed2_2.toString());
        surgery2_2.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("16:20:00")).toString());

        /** SURGERY 5 **/
        Surgery surgery2_3 = new Surgery("5", PriorityClass.A);
        LocalDateTime programmed2_3 = LocalDateTime.of(LocalDate.now(), LocalTime.parse("16:30:00"));
        surgery2_3.setProgrammedDate(programmed2_3);
        surgery2_3.setEstimatedTime(30);
        surgery2_3.addTimestamp(SurgeryEvents.InSO, programmed2_3.toString());
        surgery2_3.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("17:00:00")).toString());

        allSurgeries.add(surgery1_1);
        allSurgeries.add(surgery1_2);
        allSurgeries.add(surgery2_1);
        allSurgeries.add(surgery2_2);
        allSurgeries.add(surgery2_3);

        surgeriesExecuted.add(new SurgeryLocation("1", "1"));
        surgeriesExecuted.add(new SurgeryLocation("2", "2"));
        surgeriesExecuted.add(new SurgeryLocation("3", "1"));
        surgeriesExecuted.add(new SurgeryLocation("4", "2"));
        surgeriesExecuted.add(new SurgeryLocation("5", "1"));

        SingleSlot morningSlot = new SingleSlot("09:00:00", "12:00:00", "surgery");
        SingleSlot afternoonSlot = new SingleSlot("14:00:00", "17:00:00", "surgery");
        ArrayList<SingleSlot> slots = new ArrayList<>();
        slots.add(morningSlot);
        slots.add(afternoonSlot);
        DailySlot dailySlot = new DailySlot(LocalDate.now().toString(), slots);
        orSlots.put("1", dailySlot);
        orSlots.put("2", dailySlot);

        kpiCalculator = new KpiCalculator(allSurgeries, surgeriesExecuted, orSlots);
    }

    @Test
    void testM9() {
        float value1 = kpiCalculator.M9("1");
        Assertions.assertEquals((float) 135 /360, value1);
        float value2 = kpiCalculator.M9("2");
        Assertions.assertEquals((float) 95 /360, value2);
    }

    @Test
    void testM10() {
        float value1 = kpiCalculator.M10("1");
        Assertions.assertEquals(0, value1);
        float value2 = kpiCalculator.M10("2");
        Assertions.assertEquals(0, value2);
    }

    @Test
    void testM11OperatingRoom1() {
        /** SURGERY 6 **/
        Surgery surgery = new Surgery("6", PriorityClass.A);
        LocalDateTime programmed = LocalDateTime.of(LocalDate.now(), LocalTime.parse("11:50:00"));
        surgery.setProgrammedDate(programmed);
        surgery.addTimestamp(SurgeryEvents.InSO, programmed.toString());
        surgery.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("12:10:00")).toString());
        allSurgeries.add(surgery);
        surgeriesExecuted.add(new SurgeryLocation("6", "1"));
        float value = kpiCalculator.M11("1");
        Assertions.assertEquals(10, value);
    }

    @Test
    void testM11OperatingRoom2() {
        /** SURGERY 6 **/
        Surgery surgery = new Surgery("7", PriorityClass.A);
        LocalDateTime programmed = LocalDateTime.of(LocalDate.now(), LocalTime.parse("16:30:00"));
        surgery.setProgrammedDate(programmed);
        surgery.addTimestamp(SurgeryEvents.InSO, programmed.toString());
        surgery.addTimestamp(SurgeryEvents.OutSO, LocalDateTime.of(LocalDate.now(), LocalTime.parse("17:25:00")).toString());
        allSurgeries.add(surgery);
        surgeriesExecuted.add(new SurgeryLocation("7", "2"));
        float value = kpiCalculator.M11("2");
        Assertions.assertEquals(25, value);
    }

    @Test
    void testM12() {
        float value = kpiCalculator.M12("1");
        Assertions.assertEquals(90, value);
    }

    @Test
    void testM13() {
        float value1 = kpiCalculator.M13("1");
        Assertions.assertEquals(90, value1);
        float value2 = kpiCalculator.M13("2");
        Assertions.assertEquals(0, value2);
    }

    @Test
    void testM16() {
        float value1 = kpiCalculator.M16("1");
        Assertions.assertEquals((float) 3 / 2, value1);
        float value2 = kpiCalculator.M16("2");
        Assertions.assertEquals(1, value2);
    }

    @Test
    void testM18() {

    }

    @Test
    void testM21() {
        float value1 = kpiCalculator.M21("1");
        Assertions.assertEquals((float) 135 / 360, value1);
        float value2 = kpiCalculator.M21("2");
        Assertions.assertEquals((float) 95 / 360, value2);
    }
}
