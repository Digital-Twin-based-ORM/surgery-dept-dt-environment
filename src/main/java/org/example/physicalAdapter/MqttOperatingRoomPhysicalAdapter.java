package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.example.domain.model.fhir.LocationType;
import org.example.utils.UtilsFunctions;

import static org.example.utils.GlobalValues.*;

public class MqttOperatingRoomPhysicalAdapter extends AbstractMqttPhysicalAdapter{
    private String baseTopic = "";
    static public final String STATE = "state";
    static public final String LAST_DISINFECTION = "lastDisinfection";
    static public final String DISINFECTION_TERMINATED = "disinfectionTerminated";
    static public final String DISINFECTION_STARTED = "disinfectionStarted";
    static public final String NOW_AVAILABLE = "nowAvailable";
    static public final String BUSY = "busy";
    static public final String ASSIGN_DAILY_SLOTS = "assignDailySlots";
    static public final String ADD_NEW_SLOT = "addNewSlot";
    static public final String LOCATION_TYPE = "type";
    private final MqttPhysicalAdapterConfigurationBuilder builder;
    public MqttOperatingRoomPhysicalAdapter(String idDT, String host, Integer port) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        this.baseTopic = "anylogic/id/operatingroom/" + idDT + "/";
        this.addStringProperty(STATE, "");
        this.addStringProperty(LAST_DISINFECTION, "");

        builder.addPhysicalAssetPropertyAndTopic(LOCATION_TYPE, LocationType.SurgeryClinic, this.baseTopic + LOCATION_TYPE, content -> {
            if(LocationType.isValid(content)) {
                return LocationType.valueOf(content);
            } else {
                return LocationType.SurgeryClinic;
            }
        });

        this.addStringEvent(DISINFECTION_TERMINATED);
        this.addStringEvent(DISINFECTION_STARTED);
        this.addStringEvent(NOW_AVAILABLE);
        this.addStringEvent(BUSY);
        this.addStringProperty(TYPE, OPERATING_ROOM_TYPE);

        this.builder.addPhysicalAssetEventAndTopic(ASSIGN_DAILY_SLOTS, "text/plain", getBaseTopic() + ASSIGN_DAILY_SLOTS, i->i);
        this.builder.addPhysicalAssetEventAndTopic(ADD_NEW_SLOT, "text/plain", getBaseTopic() + ADD_NEW_SLOT, UtilsFunctions::getDailySlotsFromJson);
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
