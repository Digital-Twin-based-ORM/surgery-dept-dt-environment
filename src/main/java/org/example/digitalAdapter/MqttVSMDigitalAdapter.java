package org.example.digitalAdapter;

import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel;

public class MqttVSMDigitalAdapter {

    private MqttDigitalAdapterConfigurationBuilder builder;
    private String currentPatient = "";

    public MqttVSMDigitalAdapter(String host, Integer port, String idDT) throws MqttDigitalAdapterConfigurationException {
        this.builder = MqttDigitalAdapterConfiguration.builder(host, port);
        builder.addPropertyTopic("heartRate", "patient/" + this.currentPatient + "/heartRate", MqttQosLevel.MQTT_QOS_0, value -> String.valueOf(((Double)value).intValue()));
    }

    public String getCurrentPatient() {
        return currentPatient;
    }

    public void setCurrentPatient(String currentPatient) {
        this.currentPatient = currentPatient;
    }
}
