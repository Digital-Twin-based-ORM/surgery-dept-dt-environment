package org.example.domain.model;

import java.time.Duration;
import java.time.LocalTime;

public class SingleSlot {

    private String startSlot;
    private String endSlot;
    private String procedure;
    public SingleSlot() {
    }

    public void setStartSlot(String startSlot) {
        this.startSlot = startSlot;
    }

    public void setEndSlot(String endSlot) {
        this.endSlot = endSlot;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

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

    public float getHourDuration() {
        LocalTime start = LocalTime.parse(this.startSlot);
        LocalTime end = LocalTime.parse(this.endSlot);
        return Duration.between(start, end).toHours();
    }

    public float getMinutesDuration() {
        LocalTime start = LocalTime.parse(this.startSlot);
        LocalTime end = LocalTime.parse(this.endSlot);
        return Duration.between(start, end).toMinutes();
    }

    public boolean isSurgeryExecutedInSlot(LocalTime start, LocalTime end) {
        LocalTime thisStart = LocalTime.parse(this.startSlot);
        LocalTime thisEnd = LocalTime.parse(this.endSlot);
        return (start.isBefore(thisStart) && end.isAfter(thisEnd));
    }

    public boolean isSurgeryProgrammed(LocalTime programmedStart) {
        LocalTime thisStart = LocalTime.parse(this.startSlot);
        LocalTime thisEnd = LocalTime.parse(this.endSlot);
        return programmedStart.equals(thisStart) || (programmedStart.isAfter(thisStart) && programmedStart.isBefore(thisEnd));
    }

    public float getOverTime(LocalTime endSurgeryTime) {
        LocalTime thisEnd = LocalTime.parse(this.endSlot);
        if(endSurgeryTime.isAfter(thisEnd)) {
            return Duration.between(thisEnd, endSurgeryTime).toMinutes();
        } else {
            return 0;
        }
    }

    public float getUnderUtilizationTime(LocalTime endSurgeryTime) {
        LocalTime thisEnd = LocalTime.parse(this.endSlot);
        if(endSurgeryTime.isBefore(thisEnd)) {
            return Duration.between(endSurgeryTime, thisEnd).toMinutes();
        } else {
            return 0;
        }
    }

    public LocalTime getLocalTimeStartSlot() {
        return LocalTime.parse(this.startSlot);
    }
}
