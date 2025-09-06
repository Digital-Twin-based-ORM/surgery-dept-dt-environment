package org.example.domain.model;

import java.util.Optional;

public enum SurgeryEvents {
    OutR("OutR"),
    InF("InF"),
    InORB("InORB"),
    InSI("InSI"),
    StAnest("StAnest"),
    PzPr("PzPr"),
    InSO("InSO"),
    StCh("StCh"),
    EndCh("EndCh"),
    OutSO("OutSO"),
    OutORB("OutORB"),
    InR("InR"),
    InOutR("InOutR"),
    InOutORB("InOutORB");

    private final String name;

    SurgeryEvents(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Optional<SurgeryEvents> getPreviousEvent()  {
        if(this.ordinal() == 0) {
            return Optional.empty();
        } else {
            return Optional.of(SurgeryEvents.values()[this.ordinal() - 1]);
        }
    }
}
