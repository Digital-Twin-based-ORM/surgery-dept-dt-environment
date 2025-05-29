package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.time.LocalDate;
import java.util.Date;

public class SurgeryProperties extends InternalProperties {

    public final static String REASON_PROPERTY_KEY = "reason";
    public final static String CATEGORY_PROPERTY_KEY = "category";
    public final static String CODE_PROPERTY_KEY = "code";
    public final static String ESTIMATED_TIME_PROPERTY_KEY = "estimatedSurgeryDate";

    private final String reason;
    private final String category;
    private final String code;
    private final LocalDate estimatedSurgeryDate;

    public SurgeryProperties(String reason, String category, String code, LocalDate estimatedSurgeryDate) {
        this.reason = reason;
        this.category = category;
        this.code = code;
        this.estimatedSurgeryDate = estimatedSurgeryDate;
        this.addProperty(new PhysicalAssetProperty<>(REASON_PROPERTY_KEY, this.reason));
        this.addProperty(new PhysicalAssetProperty<>(CATEGORY_PROPERTY_KEY, this.category));
        this.addProperty(new PhysicalAssetProperty<>(CODE_PROPERTY_KEY, this.code));
        this.addProperty(new PhysicalAssetProperty<>(ESTIMATED_TIME_PROPERTY_KEY, this.estimatedSurgeryDate));
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

    public LocalDate getEstimatedSurgeryDate() {
        return estimatedSurgeryDate;
    }
}
