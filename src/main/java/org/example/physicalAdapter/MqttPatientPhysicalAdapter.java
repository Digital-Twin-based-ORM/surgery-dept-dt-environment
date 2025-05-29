package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.function.Function;

public class MqttPatientPhysicalAdapter {

    MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttPatientPhysicalAdapter(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);

        // Configuring the mqtt physical and http digital adapter
        this.addStringProperty("currentLocation", "", "anylogic/id/patient/" + idDT + "/currentLocation");
        this.addIntProperty("heartRate", 0, "patient/" + idDT + "/heartRate");
        this.addIntProperty("systolicBloodPressure", 0, "anylogic/id/patient/" + idDT + "/systolicBloodPressure");
        this.addIntProperty("diastolicBloodPressure", 0, "anylogic/id/patient/" + idDT + "/diastolicBloodPressure");

        // TODO how to manage static properties? With DT storage?
    }

    void addStringProperty(String key, String initialValue, String topic) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        builder.addPhysicalAssetPropertyAndTopic(key, initialValue, topic, i -> i);
    }

    void addIntProperty(String key, Integer initialValue, String topic) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        builder.addPhysicalAssetPropertyAndTopic(key, initialValue, topic, Integer::parseInt);
    }

    <T> void addProperty(String key, String type, String contentType, String topic, Function<T, String> topicFunction) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        builder.addPhysicalAssetActionAndTopic(key, type, contentType, topic, topicFunction);
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, builder.build());
    }

}
