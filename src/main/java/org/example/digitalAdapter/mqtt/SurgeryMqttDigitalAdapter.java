package org.example.digitalAdapter.mqtt;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.digital.topic.MqttQosLevel;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.businessLayer.adapter.SurgeryPropertyChanged;
import org.example.domain.model.PriorityClass;
import org.example.domain.model.Surgery;
import org.example.domain.model.SurgeryEventInTime;
import org.example.domain.model.SurgeryLocation;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;
import static org.example.physicalAdapter.MqttSurgeryPhysicalAdapter.*;

public class SurgeryMqttDigitalAdapter {

    MqttDigitalAdapterConfigurationBuilder builder;

    public SurgeryMqttDigitalAdapter(String host, Integer port, String idDepDT) throws MqttDigitalAdapterConfigurationException {
        builder = MqttDigitalAdapterConfiguration.builder(host, port).setAutomaticReconnectFlag(true).setConnectionTimeout(10000);
        String baseTopic = "anylogic/id/dep/" + idDepDT + "/";

        builder.addEventNotificationTopic(SURGERY_CREATED_NOTIFICATION, baseTopic + SURGERY_CREATED, MqttQosLevel.MQTT_QOS_0, this::convertSurgeryToJson);
        builder.addEventNotificationTopic(SURGERY_EVENT_KEY, baseTopic + NEW_SURGERY_EVENT, MqttQosLevel.MQTT_QOS_0, this::convertEventToJson);
        builder.addEventNotificationTopic(PRIORITY_KEY, baseTopic + SURGERY_PRIORITY_CHANGED, MqttQosLevel.MQTT_QOS_0, this::convertPriorityToJson);
        builder.addEventNotificationTopic(EXECUTED_IN_KEY, baseTopic + SURGERY_EXECUTED_IN, MqttQosLevel.MQTT_QOS_0, this::convertSurgeryExecutionToJson);
        builder.addEventNotificationTopic(IS_CANCELLED, baseTopic + SURGERY_CANCELLED, MqttQosLevel.MQTT_QOS_0, this::convertIsCancelled);
    }

    public MqttDigitalAdapter build(String id) throws MqttException, MqttDigitalAdapterConfigurationException {
        return new MqttDigitalAdapter(id, builder.build());
    }

    private String convertSurgeryToJson(Surgery surgery) {
        JsonObject obj = new JsonObject();
        obj.addProperty("idSurgery", surgery.getIdSurgery());
        obj.addProperty("arrivalDate", surgery.getArrivalDate().toString());
        obj.addProperty("admissionDate", surgery.getAdmissionDate().toString());
        obj.addProperty("programmedDate", surgery.getProgrammedDate().toString());
        obj.addProperty("priority", surgery.getPriority().toString());
        obj.addProperty("hospitalizationRegime", surgery.getHospitalizationRegime().toString());
        obj.addProperty("estimatedTime", surgery.getEstimatedTime());
        obj.addProperty("waitingListInsertionDate", surgery.getWaitingListInsertionDate().toString());
        return obj.toString();
    }

    private String convertEventToJson(SurgeryEventInTime surgeryEventInTime) {
        JsonObject obj = new JsonObject();
        obj.addProperty("event", surgeryEventInTime.event().getName());
        obj.addProperty("idSurgery", surgeryEventInTime.idSurgery());
        obj.addProperty("timestamp", surgeryEventInTime.timestamp());
        return obj.toString();
    }

    private String convertPriorityToJson(SurgeryPropertyChanged<PriorityClass> priority) {
        JsonObject obj = new JsonObject();
        obj.addProperty("idSurgery", priority.getIdSurgery());
        obj.addProperty("value", priority.getValue().toString());
        return obj.toString();
    }

    private String convertIsCancelled(SurgeryPropertyChanged<Boolean> surgery) {
        System.out.println("SURGERY CANCELLED (CONVERSION...)");
        return surgery.getIdSurgery();
    }

    private String convertSurgeryExecutionToJson(SurgeryLocation location) {
        JsonObject obj = new JsonObject();
        obj.addProperty("idSurgery", location.surgeryId());
        obj.addProperty("idOperationRoom", location.operationRoomId());
        return obj.toString();
    }
}
