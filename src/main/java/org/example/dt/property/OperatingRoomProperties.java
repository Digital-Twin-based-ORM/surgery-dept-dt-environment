package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;
import org.example.domain.model.DailySlot;
import org.example.domain.model.SingleSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OperatingRoomProperties extends InternalProperties {

    public static final String DAILY_SLOTS = "dailySlots";
    public static final String OPERATING_ROOM_NAME = "name";

    private final String name;
    private final String idRoom;

    public String getIdRoom() {
        return idRoom;
    }

    private final Map<String, DailySlot> procedureSlots;

    public OperatingRoomProperties(String name, String idRoom, Map<String, DailySlot> procedureSlots) {
        this.name = name;
        this.idRoom = idRoom;
        this.procedureSlots = procedureSlots;
        this.addProperty(new PhysicalAssetProperty<>(DAILY_SLOTS, this.procedureSlots));
        this.addProperty(new PhysicalAssetProperty<>(OPERATING_ROOM_NAME, name));
    }

    public String getName() {
        return name;
    }

    public Map<String, DailySlot> getProcedureSlots() {
        return procedureSlots;
    }
}
