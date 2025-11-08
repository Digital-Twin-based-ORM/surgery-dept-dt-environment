package org.example.domain.model.fhir;

public class CodeableConcept {
    private String code;
    private String description;

    public CodeableConcept(String code, String description) {
        this.code = code;
        this.description = description;
    }
    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
