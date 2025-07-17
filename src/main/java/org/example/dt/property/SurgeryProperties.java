package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;

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

    private final String reason;
    private final String category;
    private final String code;
    private final String creationTimestamp;

    public SurgeryProperties(String reason, String category, String code, String creationTimestamp) {
        this.reason = reason;
        this.category = category;
        this.code = code;
        this.creationTimestamp = creationTimestamp;

        this.addProperty(new PhysicalAssetProperty<>(REASON_PROPERTY_KEY, this.reason));
        this.addProperty(new PhysicalAssetProperty<>(CATEGORY_PROPERTY_KEY, this.category));
        this.addProperty(new PhysicalAssetProperty<>(CODE_PROPERTY_KEY, this.code));
        this.addProperty(new PhysicalAssetProperty<>(CREATION_TIMESTAMP_PROPERTY_KEY, this.creationTimestamp));
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
}
