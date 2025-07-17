package org.example.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * It represents the slots of a single day of operations.
 */
public class DailySlot {

    private final LocalDate day;
    private final ArrayList<SingleSlot> slots;

    public DailySlot(String day, ArrayList<SingleSlot> slots) {
        this.slots = slots;
        this.day = LocalDate.parse(day);
    }

    public LocalDate getDay() {
        return day;
    }

    public ArrayList<SingleSlot> getSlots() {
        return slots;
    }

    public boolean verifyClashes() {
        for(SingleSlot i : slots) {
            for(SingleSlot c : slots) {
                if(i.verifyClash(c)) {
                    // TODO maybe return indexes of slots that clashed
                    return true;
                }
            }
        }
        return false;
    }
}
