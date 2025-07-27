package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.domain.model.*;
import org.example.utils.UtilsFunctions;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.example.utils.UtilsFunctions.getJsonField;
import static org.example.utils.UtilsFunctions.getJsonIntField;

public class MqttSurgeryDepPhysicalAdapter extends AbstractMqttPhysicalAdapter {

    MqttPhysicalAdapterConfigurationBuilder builder;

    public static final String M10 = "m10";
    public static final String M14 = "m14";
    public static final String M15 = "m15";
    public static final String M17 = "m17";
    public static final String M26 = "m26";

    public static final String SLOT_SET = "slotset";
    public static final String WORKING_DAY_TERMINATED = "endWorkDay";
    public static final String WORKING_DAY_STARTED = "startWorkDay";
    public static final String NEW_SURGERY_EVENT = "surgeryEvent";
    public static final String SURGERY_CREATED = "surgeryCreated";
    public static final String SURGERY_PRIORITY_CHANGED = "surgeryPriorityChanged";
    public static final String SURGERY_EXECUTED_IN = "surgeryExecutedIn";

    private String baseTopic = "";

    public MqttSurgeryDepPhysicalAdapter(String idDT, String host, Integer port) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);

        this.baseTopic = "anylogic/id/dep/" + idDT + "/";

        this.addLongEvent(M10);
        this.addLongEvent(M14);
        this.addLongEvent(M15);
        this.addLongEvent(M17);
        this.addLongEvent(M26);

        this.builder.addPhysicalAssetEventAndTopic(SLOT_SET, "text/plain", this.baseTopic + SLOT_SET, UtilsFunctions::getDailySlotsFromJson);
        this.builder.addPhysicalAssetEventAndTopic(NEW_SURGERY_EVENT, "text/plain", this.baseTopic + NEW_SURGERY_EVENT, content -> {
            SurgeryEvents event = SurgeryEvents.valueOf(getJsonField(content, "event"));
            String id = getJsonField(content, "idSurgery");
            Long timestamp = Long.getLong(getJsonField(content, "timestamp"));
            return new SurgeryEventInTime(id, event, timestamp);
        });
        this.builder.addPhysicalAssetEventAndTopic(SURGERY_CREATED, "text/plain", this.baseTopic + SURGERY_CREATED, content -> {
            String id = getJsonField(content, "idSurgery");
            LocalDateTime admissionDate = LocalDateTime.parse(Objects.requireNonNull(getJsonField(content, "admissionDate")));
            LocalDateTime programmedDate = LocalDateTime.parse(Objects.requireNonNull(getJsonField(content, "programmedDate")));
            Integer priority = getJsonIntField(content, "priority");
            HospitalizationRegime hospitalizationRegime = HospitalizationRegime.valueOf(getJsonField(content, "hospitalizationRegime"));
            assert priority != null;
            return new Surgery(id, programmedDate, admissionDate, PriorityClass.values()[priority], hospitalizationRegime);
        });

        this.builder.addPhysicalAssetEventAndTopic(SURGERY_PRIORITY_CHANGED, "text/plain", this.baseTopic + SURGERY_PRIORITY_CHANGED, content -> {
            String id = getJsonField(content, "idSurgery");
            Integer priority = getJsonIntField(content, "priority");
            assert priority != null;
            return new Surgery(id, PriorityClass.values()[priority]);
        });

        this.builder.addPhysicalAssetEventAndTopic(SURGERY_EXECUTED_IN, "text/plain", this.baseTopic + SURGERY_EXECUTED_IN, content -> {
            String idSurgery = getJsonField(content, "idSurgery");
            String idOperationRoom = getJsonField(content, "idOperationRoom");
            return new SurgeryLocation(idSurgery, idOperationRoom);
        });

        this.addStringEvent(WORKING_DAY_STARTED);
        this.addStringEvent(WORKING_DAY_TERMINATED);
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, builder.build());
    }

    @Override
    public String getBaseTopic() {
        return baseTopic;
    }

    @Override
    public MqttPhysicalAdapterConfigurationBuilder getBuilder() {
        return builder;
    }
}
