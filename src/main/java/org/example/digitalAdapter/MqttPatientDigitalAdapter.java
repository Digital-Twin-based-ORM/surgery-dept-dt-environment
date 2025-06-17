package org.example.digitalAdapter;

import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttPatientDigitalAdapter {

    MqttDigitalAdapterConfigurationBuilder builder;

    public MqttPatientDigitalAdapter(String host, Integer port, String idDT) throws MqttDigitalAdapterConfigurationException {
        builder = MqttDigitalAdapterConfiguration.builder(host, port);
        builder.addEventNotificationTopic("bpmAnomaly", "anylogic/id/Patient/" + idDT + "/notifications/bpmAnomaly", MqttQosLevel.MQTT_QOS_0, Object::toString);
        builder.addEventNotificationTopic("newHeartRate", "anylogic/id/Patient/" + idDT + "/notifications/bpm", MqttQosLevel.MQTT_QOS_0, Object::toString);
    }

    public MqttDigitalAdapter build(String id) throws MqttException, MqttDigitalAdapterConfigurationException {
        return new MqttDigitalAdapter(id, builder.build());
    }
}
