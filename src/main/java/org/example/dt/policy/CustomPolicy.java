package org.example.dt.policy;

import java.util.function.Function;

public class CustomPolicy <T> {
    private final Function<T, Boolean> handler;
    private T lastValue;

    public void setLastValue(T lastValue) {
        this.lastValue = lastValue;
    }

    public Class<?> getClassType() {
        return lastValue.getClass();
    }

    public CustomPolicy(Function<T, Boolean> handler, T currentValue) {
        this.handler = handler;
        this.lastValue = currentValue;
    }

    public boolean canBeModified() {
        if(lastValue == null) {
            return true;
        }
        return handler.apply(lastValue);
    }
}
