package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;
import org.example.domain.model.DailySlot;
import org.example.domain.model.SingleSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Map;

public class OperatingRoomProperties extends InternalProperties {

    public static final String DAILY_SLOTS = "dailySlots";
    public static final String OPERATING_ROOM_NAME = "name";

    private final String name;

    private final Map<String, DailySlot> procedureSlots;

    public OperatingRoomProperties(String name, Map<String, DailySlot> procedureSlots) {
        this.name = name;
        this.procedureSlots = procedureSlots;
        SingleSlot slot = new SingleSlot(LocalTime.now().toString(), LocalTime.now().plusHours(1).toString(), "");
        ArrayList<SingleSlot> slots = new ArrayList<>();
        slots.add(slot);
        procedureSlots.put(LocalDate.now().toString(), new DailySlot(LocalDate.now().toString(), slots));
        this.addProperty(new PhysicalAssetProperty<>(DAILY_SLOTS, this.procedureSlots));
        this.addProperty(new PhysicalAssetProperty<>(OPERATING_ROOM_NAME, this.procedureSlots));
    }

    public String getName() {
        return name;
    }

    public Map<String, DailySlot> getProcedureSlots() {
        return procedureSlots;
    }
}
