package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.function.Function;

public class MqttPatientPhysicalAdapter {

    MqttPhysicalAdapterConfigurationBuilder builder;

    private String host;
    private Integer port;
    private String idDT;

    public MqttPatientPhysicalAdapter(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.host = host;
        this.port = port;
        this.idDT = idDT;
        this.builder = MqttPhysicalAdapterConfiguration.builder(this.host, this.port);

        // Configuring the mqtt physical and http digital adapter
        this.addStringProperty("currentLocation", "", "anylogic/id/patient/" + this.idDT + "/currentLocation");
        this.addIntProperty("heartRate", 0, "patient/" + this.idDT + "/heartRate");
        this.addIntProperty("systolicBloodPressure", 0, "anylogic/id/patient/" + this.idDT + "/systolicBloodPressure");
        this.addIntProperty("diastolicBloodPressure", 0, "anylogic/id/patient/" + this.idDT + "/diastolicBloodPressure");
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
