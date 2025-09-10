package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;
import org.example.domain.model.HospitalizationRegime;
import org.example.domain.model.PriorityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.function.Supplier;

public class SurgeryProperties extends InternalProperties {

    public final static String REASON_PROPERTY_KEY = "reason";
    public final static String CATEGORY_PROPERTY_KEY = "category";
    public final static String CODE_PROPERTY_KEY = "code";
    public final static String CREATION_TIMESTAMP_PROPERTY_KEY = "creationTimestamp";
    public final static String SCORE_KEY = "score";
    public final static String ADMISSION_TIME_KEY = "admissionTime";
    public final static String PROGRAMMED_DATE_KEY = "programmed_date";

    private final String reason;
    private final String category;
    private final String code;
    private final String creationTimestamp;
    private final String admissionDate;
    private final String programmedDate;
    private final PriorityClass priority;
    private final HospitalizationRegime regime;

    public HospitalizationRegime getRegime() {
        return regime;
    }

    public PriorityClass getPriority() {
        return priority;
    }

    public LocalDateTime getProgrammedDate() {
        return LocalDateTime.parse(programmedDate);
    }

    public SurgeryProperties(String reason, String category, String code, String creationTimestamp, String admissionDate, String programmedDate, PriorityClass priority, HospitalizationRegime regime) {
        this.reason = reason;
        this.category = category;
        this.code = code;
        this.creationTimestamp = creationTimestamp;
        this.admissionDate = admissionDate;
        this.programmedDate = programmedDate;
        this.priority = priority;
        this.regime = regime;

        this.addProperty(new PhysicalAssetProperty<>(REASON_PROPERTY_KEY, this.reason));
        this.addProperty(new PhysicalAssetProperty<>(CATEGORY_PROPERTY_KEY, this.category));
        this.addProperty(new PhysicalAssetProperty<>(CODE_PROPERTY_KEY, this.code));
        this.addProperty(new PhysicalAssetProperty<>(CREATION_TIMESTAMP_PROPERTY_KEY, this.creationTimestamp));
        this.addProperty(new PhysicalAssetProperty<>(ADMISSION_TIME_KEY, this.admissionDate));
        this.addProperty(new PhysicalAssetProperty<>(PROGRAMMED_DATE_KEY,  this.programmedDate));
    }

    public String getReason() {
        return reason;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getCreationTimestamp() {
        return creationTimestamp;
    }

    public LocalDateTime getAdmissionDate() {
        return LocalDateTime.parse(admissionDate);
    }
}
