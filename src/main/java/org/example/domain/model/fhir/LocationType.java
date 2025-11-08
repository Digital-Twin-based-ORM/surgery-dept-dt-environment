package org.example.domain.model.fhir;

import java.util.Arrays;

public enum LocationType {
    SurgeryClinic("SU"),
    EmergencyRoom("ER"),
    OutpatientFacility("OF")
    ;
    public final String code;
    private LocationType(String code) {
        this.code = code;
    }
    public static boolean isValid(String code) {
        boolean isEmpty = Arrays.stream(LocationType.values()).filter(i ->
                i.code.equals(code)
        ).toList().isEmpty();
        return !isEmpty;
    }
}
