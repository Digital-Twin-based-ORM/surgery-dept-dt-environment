package org.example.digitalAdapter.mqtt;

import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.utils.UtilsFunctions;

import static org.example.physicalAdapter.MqttOperatingRoomPhysicalAdapter.ASSIGN_DAILY_SLOTS;
import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.SLOT_SET;

public class OperatingRoomMqttDigitalAdapter {

    MqttDigitalAdapterConfigurationBuilder builder;

    public OperatingRoomMqttDigitalAdapter(String host, Integer port, String idDepDT) throws MqttDigitalAdapterConfigurationException {
        builder = MqttDigitalAdapterConfiguration.builder(host, port);
        String baseTopic = "anylogic/id/dep/" + idDepDT + "/";

        builder.addEventNotificationTopic(ASSIGN_DAILY_SLOTS, baseTopic + SLOT_SET, MqttQosLevel.MQTT_QOS_0, UtilsFunctions::convertSlotToJson);
    }

    public MqttDigitalAdapter build(String id) throws MqttException, MqttDigitalAdapterConfigurationException {
        return new MqttDigitalAdapter(id, builder.build());
    }
}
