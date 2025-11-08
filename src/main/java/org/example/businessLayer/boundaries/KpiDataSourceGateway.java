package org.example.businessLayer.boundaries;

import org.example.businessLayer.adapter.KpiSubject;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface KpiDataSourceGateway {
    boolean addOperatingRoomKpiRecord(String id, String type, float value, LocalDateTime timestamp);
    boolean addSurgeryKpiRecord(String id, String type, String surgeryType, float value, LocalDateTime timestamp);
    boolean addM22Kpi(float percentage, LocalDate date);
    boolean checkTableExist(String tableName);

}
