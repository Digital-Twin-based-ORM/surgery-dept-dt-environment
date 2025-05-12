package org.example.domain.model;

public enum PriorityClass {

    A(30, DateType.DAY),
    B(60, DateType.DAY),
    C(180,DateType.DAY),
    D(12, DateType.MONTH);

    public final int maxTime;
    private final DateType dateType;

    private PriorityClass(int maxTime, DateType dateType) {
        this.maxTime = maxTime;
        this.dateType = dateType;
    }

    public int getMaxTime() {
        return maxTime;
    }

    public DateType getDateType() {
        return dateType;
    }
}
