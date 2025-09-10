package org.example.physicalAdapter;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.businessLayer.adapter.OperatingRoomDailySlot;
import org.example.businessLayer.adapter.SurgeryKpiNotification;
import org.example.domain.model.*;
import org.example.utils.UtilsFunctions;

import java.time.LocalDateTime;
import java.util.Objects;

import static org.example.utils.UtilsFunctions.getJsonField;
import static org.example.utils.UtilsFunctions.getJsonIntField;

public class MqttSurgeryDepPhysicalAdapter extends AbstractMqttPhysicalAdapter {

    MqttPhysicalAdapterConfigurationBuilder builder;

    public static final String M1 = "m1";
    public static final String M2 = "m2";
    public static final String M3 = "m3";
    public static final String M9 = "m9";
    public static final String M10 = "m10";
    public static final String M11 = "m11";
    public static final String M12 = "m12";
    public static final String M13 = "m13";
    public static final String M14 = "m14";
    public static final String M15 = "m15";
    public static final String M16 = "m16";
    public static final String M17 = "m17";
    public static final String M18 = "m18";
    public static final String M19 = "m19";
    public static final String M20 = "m20";
    public static final String M21 = "m21";
    public static final String M22 = "m22";
    public static final String M23 = "m23";
    public static final String M24 = "m24";
    public static final String M26 = "m26";
    public static final String M29 = "m29";

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

        this.addSurgeryKpiEventTopic(M10);
        this.addSurgeryKpiEventTopic(M14);
        this.addSurgeryKpiEventTopic(M15);
        this.addSurgeryKpiEventTopic(M17);
        this.addSurgeryKpiEventTopic(M26);

        this.builder.addPhysicalAssetEventAndTopic(SLOT_SET, "text/plain", this.baseTopic + SLOT_SET, content -> {
            // get the daily slots of the current working day and the specific operating room
            JsonObject jsonObject = UtilsFunctions.stringToJsonObjectGson(content);
            String operatingRoomId = jsonObject.get("operatingRoomId").getAsString();
            DailySlot slots = UtilsFunctions.getDailySlotsFromJson(jsonObject);
            return new OperatingRoomDailySlot(operatingRoomId, slots);
        });
        this.builder.addPhysicalAssetEventAndTopic(NEW_SURGERY_EVENT, "text/plain", this.baseTopic + NEW_SURGERY_EVENT, UtilsFunctions::surgeryEventInTimeFromJson);

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

    public void addSurgeryKpiEventTopic(String kpi) throws MqttPhysicalAdapterConfigurationException {
        this.builder.addPhysicalAssetEventAndTopic(kpi, "text/plain", this.baseTopic + kpi, it -> {
            JsonObject json = UtilsFunctions.stringToJsonObjectGson(it);
            assert json != null;
            return new SurgeryKpiNotification(json.get("surgeryId").getAsString(), json.get("value").getAsFloat());
        });
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
