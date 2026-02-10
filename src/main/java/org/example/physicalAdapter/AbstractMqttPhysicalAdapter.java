package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.time.LocalDateTime;

public abstract class AbstractMqttPhysicalAdapter {
    void addStringProperty(String key, String initialValue) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        // i -> getJsonField(i, STATUS_KEY) alternative with json
        getBuilder().addPhysicalAssetPropertyAndTopic(key, initialValue, getBaseTopic() + key, String::toString);
    }

    void addBooleanProperty(String key, boolean initialValue) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        // i -> getJsonField(i, STATUS_KEY) alternative with json
        getBuilder().addPhysicalAssetPropertyAndTopic(key, initialValue, getBaseTopic() + key, Boolean::parseBoolean);
    }

    void addStringEvent(String key) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        // i -> getJsonField(i, STATUS_KEY) alternative with json
        getBuilder().addPhysicalAssetEventAndTopic(key, "text/plain", getBaseTopic() + key, String::toString);
    }

    void addDoublePropery(String key, double initialValue) throws MqttPhysicalAdapterConfigurationException {
        getBuilder().addPhysicalAssetPropertyAndTopic(key, initialValue, getBaseTopic() + key, Float::parseFloat);
    }

    void addLongEvent(String key) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        // i -> getJsonField(i, STATUS_KEY) alternative with json
        getBuilder().addPhysicalAssetEventAndTopic(key, "text/plain", getBaseTopic() + key, Long::getLong);
    }

    void addLocalDateTimeProperty(String key, String initialValue) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        // LocalDateTime.parse(Objects.requireNonNull(getJsonField(i, PROGRAMMED_DATE_KEY))) alternative with json
        getBuilder().addPhysicalAssetPropertyAndTopic(key, initialValue, getBaseTopic() + key, LocalDateTime::parse);
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, getBuilder().build());
    }

    public abstract String getBaseTopic();
    public abstract MqttPhysicalAdapterConfigurationBuilder getBuilder();
}
