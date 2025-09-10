package org.example.digitalAdapter.mqtt;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.domain.model.Surgery;
import org.example.domain.model.SurgeryEventInTime;
import org.example.utils.Pair;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;
import static org.example.physicalAdapter.MqttSurgeryPhysicalAdapter.*;

public class SurgeryMqttDigitalAdapter {

    MqttDigitalAdapterConfigurationBuilder builder;

    public SurgeryMqttDigitalAdapter(String host, Integer port, String idDT, String idDepDT) throws MqttDigitalAdapterConfigurationException {
        builder = MqttDigitalAdapterConfiguration.builder(host, port);
        String baseTopic = "anylogic/id/dep/" + idDepDT + "/";

        builder.addEventNotificationTopic(M10, baseTopic + M10, MqttQosLevel.MQTT_QOS_0, this::convertKpiToJson);
        builder.addEventNotificationTopic(M14, baseTopic + M14, MqttQosLevel.MQTT_QOS_0, this::convertKpiToJson);
        builder.addEventNotificationTopic(M15, baseTopic + M15, MqttQosLevel.MQTT_QOS_0, this::convertKpiToJson);
        builder.addEventNotificationTopic(M17, baseTopic + M17, MqttQosLevel.MQTT_QOS_0, this::convertKpiToJson);
        builder.addEventNotificationTopic(M26, baseTopic + M26, MqttQosLevel.MQTT_QOS_0, this::convertKpiToJson);

        builder.addEventNotificationTopic(SURGERY_CREATED_NOTIFICATION, baseTopic + SURGERY_CREATED, MqttQosLevel.MQTT_QOS_0, this::convertSurgeryToJson);
        builder.addEventNotificationTopic(SURGERY_EVENT_KEY, baseTopic + NEW_SURGERY_EVENT, MqttQosLevel.MQTT_QOS_0, this::convertEventToJson);
        builder.addEventNotificationTopic(PRIORITY_KEY, baseTopic + SURGERY_PRIORITY_CHANGED, MqttQosLevel.MQTT_QOS_0, this::convertPriorityToJson);
        builder.addEventNotificationTopic(EXECUTED_IN_KEY, baseTopic + SURGERY_EXECUTED_IN, MqttQosLevel.MQTT_QOS_0, this::convertSurgeryExecutionToJson);

    }

    public MqttDigitalAdapter build(String id) throws MqttException, MqttDigitalAdapterConfigurationException {
        return new MqttDigitalAdapter(id, builder.build());
    }

    private String convertKpiToJson(Pair<String, String> kpi) {
        JsonObject obj = new JsonObject();
        obj.addProperty("surgeryId", kpi.getLeft());
        obj.addProperty("value", kpi.getRight());
        return obj.toString();
    }

    private String convertSurgeryToJson(Surgery surgery) {
        JsonObject obj = new JsonObject();
        obj.addProperty("idSurgery", surgery.getIdSurgery());
        obj.addProperty("admissionDate", surgery.getAdmissionDate().toString());
        obj.addProperty("programmedDate", surgery.getProgrammedDate().toString());
        obj.addProperty("priority", surgery.getPriority().toString());
        obj.addProperty("hospitalizationRegime", surgery.getHospitalizationRegime().toString());
        return obj.toString();
    }

    private String convertEventToJson(SurgeryEventInTime surgeryEventInTime) {
        JsonObject obj = new JsonObject();
        obj.addProperty("event", surgeryEventInTime.event().getName());
        obj.addProperty("idSurgery", surgeryEventInTime.idSurgery());
        obj.addProperty("timestamp", surgeryEventInTime.timestamp().toString());
        return obj.toString();
    }

    private String convertPriorityToJson(Pair<String, String> priority) {
        JsonObject obj = new JsonObject();
        obj.addProperty("idSurgery", priority.getLeft());
        obj.addProperty("priority", priority.getRight());
        return obj.toString();
    }

    private String convertSurgeryExecutionToJson(Pair<String, String> surgery) {
        JsonObject obj = new JsonObject();
        obj.addProperty("idSurgery", surgery.getLeft());
        obj.addProperty("idOperationRoom", surgery.getRight());
        return obj.toString();
    }
}
