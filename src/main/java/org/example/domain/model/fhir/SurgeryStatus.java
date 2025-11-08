package org.example.domain.model.fhir;

import java.util.Arrays;

public enum SurgeryStatus {
    PREPARATION("preparation"),
    IN_PROGRESS("in-progress"),
    NOT_DONE("not-done"),
    ON_HOLD("on-hold"),
    STOPPED("stopped"),
    COMPLETED("completed"),
    ENTERED_IN_ERROR("entered-in-error"),
    UNKNOWN("unknown")
    ;

    public final String code;

    private SurgeryStatus(String code) {
        this.code = code;
    }

    public static boolean isValid(String code) {
        boolean isEmpty = Arrays.stream(SurgeryStatus.values()).filter(i ->
            i.code.equals(code)
        ).toList().isEmpty();
        return !isEmpty;
    }
}
