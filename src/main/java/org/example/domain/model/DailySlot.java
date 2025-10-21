package org.example.domain.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * It represents the slots of a single day of operations.
 */
public class DailySlot {
    public String day;
    public final ArrayList<SingleSlot> slots;

    public DailySlot() {
        this.day = "";
        this.slots = new ArrayList<>();
    }

    public DailySlot(String day, ArrayList<SingleSlot> slots) {
        this.slots = slots;
        this.day = day;
    }

    @Override
    public String toString() {
        return "DailySlot{" +
                "day='" + day + '\'' +
                ", slots=" + slots +
                '}';
    }

    public LocalDate getLocalDateDay() {
        DateTimeFormatter parser = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(day, parser);
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
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

    public boolean isDayEqualsTo(LocalDate date) {
        LocalDate slotDay = getLocalDateDay();
        return slotDay.getMonth().equals(date.getMonth()) && slotDay.getDayOfMonth() == date.getDayOfMonth() && slotDay.getYear() == date.getYear();
    }
}
