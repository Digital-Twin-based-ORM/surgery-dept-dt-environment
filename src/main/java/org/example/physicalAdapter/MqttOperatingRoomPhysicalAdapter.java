package org.example.physicalAdapter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.example.domain.model.DailySlot;
import org.example.domain.model.SingleSlot;
import org.example.utils.UtilsFunctions;

import java.util.ArrayList;

import static org.example.utils.UtilsFunctions.stringToJsonObjectGson;

public class MqttOperatingRoomPhysicalAdapter extends AbstractMqttPhysicalAdapter{
    private String baseTopic = "";
    static public final String AVAILABILITY = "availability";
    static public final String STATE = "state";
    static public final String LAST_DISINFECTION = "lastDisinfection";
    static public final String DISINFECTION_TERMINATED = "disinfectionTerminated";
    static public final String NOW_AVAILABLE = "nowAvailable";
    static public final String BUSY = "busy";
    static public final String ASSIGN_DAILY_SLOTS = "assignDailySlots";
    private final MqttPhysicalAdapterConfigurationBuilder builder;
    public MqttOperatingRoomPhysicalAdapter(String idDT, String host, Integer port) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        this.baseTopic = "anylogic/id/operatingroom/" + idDT + "/";
        this.addStringProperty(AVAILABILITY, "");
        this.addStringProperty(STATE, "");
        this.addStringProperty(LAST_DISINFECTION, "");

        this.addStringEvent(DISINFECTION_TERMINATED);
        this.addStringEvent(NOW_AVAILABLE);
        this.addStringEvent(BUSY);

        this.builder.addPhysicalAssetEventAndTopic(ASSIGN_DAILY_SLOTS, "text/plain", getBaseTopic() + ASSIGN_DAILY_SLOTS, UtilsFunctions::getDailySlotsFromJson);
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
