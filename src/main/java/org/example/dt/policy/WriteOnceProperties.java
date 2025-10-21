package org.example.dt.policy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class WriteOnceProperties {

    private final Map<String, Boolean> writeOnceProperties = new HashMap<>();
    private final Map<String, CustomPolicy<?>> customPolicies = new HashMap<>();

    public void addKeys(String... keys) {
        for(String key : keys) {
            this.writeOnceProperties.put(key, false);
        }
    }

    public void addKey(String key) {
        this.writeOnceProperties.put(key, false);
    }

    public void setModified(String key) {
        if(this.writeOnceProperties.containsKey(key))
            this.writeOnceProperties.put(key, true);
    }

    public boolean isModified(String key) {
        if(!this.writeOnceProperties.containsKey(key)) {
            return false;
        }
        return this.writeOnceProperties.get(key);
    }

    public <T> void addCustomPolicy(String key, Function<T, Boolean> handler, T currentValue) {
        this.customPolicies.put(key, new CustomPolicy<>(handler, currentValue));
    }

    public <T> void setLastValueForCustomPolicy(String key, T currentValue) {
        if(this.customPolicies.containsKey(key)) {
            System.out.println("VALUE TYPE: " + currentValue.getClass());
            System.out.println("POLICY TYPE: " + this.customPolicies.get(key).getClassType());
            if(currentValue.getClass().equals(this.customPolicies.get(key).getClassType())) {
                System.out.println("Check");
                @SuppressWarnings("unchecked")
                CustomPolicy<T> policy = (CustomPolicy<T>) this.customPolicies.get(key);
                if(policy != null) {
                    System.out.println("SET LAST VALUE TO: " + currentValue);
                    policy.setLastValue(currentValue);
                }
            }
            else {
                throw new ClassCastException();
            }
        }
    }

    public boolean isModificationAllowed(String key) {
        CustomPolicy<?> policy = (CustomPolicy<?>) this.customPolicies.get(key);
        if(policy != null) {
            return policy.canBeModified();
        } else {
            return true;
        }
    }
}
