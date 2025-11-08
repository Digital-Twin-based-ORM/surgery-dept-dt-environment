package org.example.domain.model.fhir;

import java.util.Arrays;

public enum DeviceStatus {

    ACTIVE("active"),
    INACTIVE("inactive"),
    ENTERED_IN_ERROR("entered-in-error")
    ;

    public final String code;

    private DeviceStatus(String code) {
        this.code = code;
    }

    public static boolean isValid(String code) {
        boolean isEmpty = Arrays.stream(DeviceStatus.values()).filter(i ->
                i.code.equals(code)
        ).toList().isEmpty();
        return !isEmpty;
    }
}
