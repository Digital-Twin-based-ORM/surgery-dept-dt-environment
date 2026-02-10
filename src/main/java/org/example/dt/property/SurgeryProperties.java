package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;
import org.example.domain.model.HospitalizationRegime;
import org.example.domain.model.PriorityClass;
import org.example.domain.model.fhir.CodeableConcept;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Supplier;

import static org.example.utils.GlobalValues.IDENTIFIER_KEY;

public class SurgeryProperties extends InternalProperties {

    public final static String REASON_PROPERTY_KEY = "reason";
    public final static String CATEGORY_PROPERTY_KEY = "category";
    public final static String CODE_PROPERTY_KEY = "code";
    public final static String CREATION_TIMESTAMP_PROPERTY_KEY = "creationTimestamp";
    public final static String SCORE_KEY = "score"; // TODO
    public final static String ADMISSION_TIME_KEY = "admissionTime";

    private final CodeableConcept reason;
    private final CodeableConcept category; // procedure
    private final CodeableConcept code;
    private final String creationTimestamp;
    private final String admissionDate;
    private final String programmedDate;
    private final PriorityClass priority;
    private final HospitalizationRegime regime;
    private final int estimatedTime;
    private final String arrivalDate;
    private final String waitingListInsertionDate;
    private final String identifier;

    public HospitalizationRegime getRegime() {
        return regime;
    }

    public PriorityClass getPriority() {
        return priority;
    }

    public String getProgrammedDate() {
        return programmedDate;
    }

    public SurgeryProperties(CodeableConcept reason, CodeableConcept category, CodeableConcept code, String creationTimestamp, String admissionDate, String programmedDate, PriorityClass priority, HospitalizationRegime regime, int estimatedTime, String arrivalDate, String waitingListInsertionDate, String identifier) {
        this.reason = reason;
        this.category = category;
        this.code = code;
        this.creationTimestamp = creationTimestamp;
        this.admissionDate = admissionDate;
        this.priority = priority;
        this.regime = regime;
        this.programmedDate = programmedDate;
        this.estimatedTime = estimatedTime;
        this.arrivalDate = arrivalDate;
        this.waitingListInsertionDate = waitingListInsertionDate;
        this.identifier = identifier;

        this.addProperty(new PhysicalAssetProperty<>(REASON_PROPERTY_KEY, this.reason));
        this.addProperty(new PhysicalAssetProperty<>(CATEGORY_PROPERTY_KEY, this.category));
        this.addProperty(new PhysicalAssetProperty<>(CODE_PROPERTY_KEY, this.code));
        this.addProperty(new PhysicalAssetProperty<>(CREATION_TIMESTAMP_PROPERTY_KEY, this.creationTimestamp));
        this.addProperty(new PhysicalAssetProperty<>(ADMISSION_TIME_KEY, this.admissionDate));
        this.addProperty(new PhysicalAssetProperty<>(IDENTIFIER_KEY, this.identifier));
    }

    public int getEstimatedTime() {
        return estimatedTime;
    }

    public CodeableConcept getReason() {
        return reason;
    }

    public LocalDateTime getArrivalDate() {
        return LocalDateTime.parse(arrivalDate);
    }

    public CodeableConcept getCategory() {
        return category;
    }

    public CodeableConcept getCode() {
        return code;
    }

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public LocalDateTime getWaitingListInsertionDate() {
        return LocalDateTime.parse(waitingListInsertionDate);
    }

    public LocalDateTime getAdmissionDate() {
        return LocalDateTime.parse(admissionDate);
    }
    public LocalDateTime getLocalProgrammedDate() {
        return LocalDateTime.parse(programmedDate);
    }
}
