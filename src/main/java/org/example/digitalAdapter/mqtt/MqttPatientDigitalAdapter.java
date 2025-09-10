package org.example.digitalAdapter.mqtt;

import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPatientDigitalAdapter {

    public final static String BPM_ANOMALY = "bpmAnomaly";
    public final static String NEW_HEART_RATE = "bpm";

    MqttDigitalAdapterConfigurationBuilder builder;

    public MqttPatientDigitalAdapter(String host, Integer port, String idDT) throws MqttDigitalAdapterConfigurationException {
        builder = MqttDigitalAdapterConfiguration.builder(host, port);
        builder.addEventNotificationTopic(BPM_ANOMALY, "anylogic/id/Patient/" + idDT + "/notifications/" + BPM_ANOMALY, MqttQosLevel.MQTT_QOS_0, Object::toString);
        builder.addEventNotificationTopic(NEW_HEART_RATE, "anylogic/id/Patient/" + idDT + "/notifications/" + NEW_HEART_RATE, MqttQosLevel.MQTT_QOS_0, Object::toString);
    }

    public MqttDigitalAdapter build(String id) throws MqttException, MqttDigitalAdapterConfigurationException {
        return new MqttDigitalAdapter(id, builder.build());
    }
}
