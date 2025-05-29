package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;

import java.util.ArrayList;

public class InternalProperties {

    protected final ArrayList<PhysicalAssetProperty<?>> properties = new ArrayList<>();

    public void addProperty(PhysicalAssetProperty<?> value) {
        value.setImmutable(true);
        properties.add(value);
    }

    public ArrayList<PhysicalAssetProperty<?>> getProperties() {
        return properties;
    }
}
