package org.example.domain.model;

import java.time.LocalTime;

public class SingleSlot {

    private String startSlot;
    private String endSlot;

    private String procedure;

    public SingleSlot(String startSlot, String endSlot, String procedure) {
        this.startSlot = startSlot;
        this.endSlot = endSlot;
        this.procedure = procedure;
    }

    public String getStartSlot() {
        return startSlot;
    }

    public String getEndSlot() {
        return endSlot;
    }

    public String getProcedure() {
        return procedure;
    }

    public boolean verifyClash(SingleSlot slot) {
        LocalTime thisStart = LocalTime.parse(this.startSlot);
        LocalTime thisEnd = LocalTime.parse(this.endSlot);
        LocalTime slotStart = LocalTime.parse(slot.startSlot);
        LocalTime SlotEnd = LocalTime.parse(slot.endSlot);
        if(slotStart.isAfter(thisStart) && slotStart.isBefore(thisEnd)) {
            return true;
        }
        if(SlotEnd.isAfter(thisStart) && SlotEnd.isBefore(thisEnd)) {
            return true;
        }
        return false;
    }
}
