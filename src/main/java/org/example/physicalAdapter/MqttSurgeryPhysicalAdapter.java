package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.example.businessLayer.adapter.OperatingRoomInfo;
import org.example.domain.model.PriorityClass;
import org.example.domain.model.SurgeryEventInTime;
import org.example.domain.model.SurgeryEvents;
import org.example.domain.model.fhir.SurgeryStatus;
import org.example.dt.property.SurgeryProperties;

import static org.example.utils.GlobalValues.*;
import static org.example.utils.UtilsFunctions.getJsonField;

public class MqttSurgeryPhysicalAdapter extends AbstractMqttPhysicalAdapter {

    // TODO add cancelSurgery event: set status as cancelled and notify department
    public final static String PRIORITY_KEY = "priority";
    public final static String STATUS_KEY = "status";
    public final static String LAST_EVENT_KEY = "last_event";
    public final static String PATIENT_ID_KEY = "patient_id";
    public final static String EXECUTION_START_KEY = "execution_start_date";
    public final static String EXECUTION_END_KEY = "execution_end_date";
    public final static String WARNING_KEY = "warning";
    public final static String SURGERY_EVENT_KEY = "updateSurgeryState";
    public final static String SURGERY_INCISION_KEY = "incision_timestamp";
    public final static String SURGERY_SUTURE_KEY = "suture_timestamp";
    public final static String PROGRAMMED_IN_KEY = "programmedIn";
    public final static String EXECUTED_IN_KEY = "executedIn";
    public final static String SURGERY_TERMINATED = "surgeryTerminated";
    public final static String PROGRAMMED_DATE = "programmed_date";
    public final static String SURGERY_CREATED_NOTIFICATION = "surgeryCreatedNotification";
    public final static String ESTIMATED_TIME_KEY = "estimated_time";
    public final static String IS_CANCELLED = "is_cancelled";
    public final static String HOSPITALIZATION_REGIME_KEY = "hospitalizationRegime";
    public final static String WAITING_LIST_INSERTION_KEY = "waitingListInsertionDate";
    public final static String IS_DONE_KEY = "isDone";

    private String baseTopic = "";
    private final MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttSurgeryPhysicalAdapter(String idDT, String host, Integer port, SurgeryProperties properties) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        this.baseTopic = "anylogic/id/surgery/" + idDT + "/";

        builder.addPhysicalAssetEventAndTopic(SURGERY_EVENT_KEY, "text/plain", this.baseTopic + SURGERY_EVENT_KEY, content -> {
            SurgeryEvents event = SurgeryEvents.valueOf(getJsonField(content, "event"));
            String timestamp = getJsonField(content, "timestamp");
            return new SurgeryEventInTime(idDT, event, timestamp);
        });

        builder.addPhysicalAssetPropertyAndTopic(PRIORITY_KEY, properties.getPriority(), this.baseTopic + PRIORITY_KEY, i -> {
            if(i.equals(PriorityClass.A.toString()) || i.equals(PriorityClass.B.toString()) || i.equals(PriorityClass.C.toString()) || i.equals(PriorityClass.D.toString()))
                return i;
            else
                throw new IllegalArgumentException();
        });

        builder.addPhysicalAssetPropertyAndTopic(STATUS_KEY, SurgeryStatus.UNKNOWN, this.baseTopic + STATUS_KEY, content -> {
            if(SurgeryStatus.isValid(content)) {
                return SurgeryStatus.valueOf(content);
            } else {
                return SurgeryStatus.UNKNOWN;
            }
        });

        this.addStringEvent(WARNING_KEY);
        this.addStringEvent(PATIENT_ID_KEY);
        this.addStringEvent(PROGRAMMED_IN_KEY);
        this.addStringEvent(SURGERY_TERMINATED);
        this.addStringEvent(SURGERY_CREATED_NOTIFICATION);
        this.addStringProperty(TYPE, SURGERY_TYPE);

        this.addStringProperty(HOSPITALIZATION_REGIME_KEY, properties.getRegime().toString());
        this.addStringProperty(WAITING_LIST_INSERTION_KEY, properties.getWaitingListInsertionDate().toString());
        this.addBooleanProperty(IS_DONE_KEY, false);

        this.addStringProperty(EXECUTION_START_KEY, ""); // redundant (not necessary, only for tracking reason)
        this.addStringProperty(EXECUTION_END_KEY, ""); // redundant (not necessary, only for tracking reason)
        this.addStringProperty(SURGERY_INCISION_KEY, ""); // redundant (not necessary, only for tracking reason)
        this.addStringProperty(SURGERY_SUTURE_KEY, ""); // redundant (not necessary, only for tracking reason)
        this.addStringProperty(PROGRAMMED_DATE, properties.getProgrammedDate());
        this.addStringProperty(LAST_EVENT_KEY, "");
        this.addStringProperty(ESTIMATED_TIME_KEY, String.format("%d", properties.getEstimatedTime()));

        this.addBooleanProperty(IS_CANCELLED, false);

        builder.addPhysicalAssetEventAndTopic(EXECUTED_IN_KEY, "text/plain", this.baseTopic + EXECUTED_IN_KEY, content -> {
            String id = getJsonField(content, "id");
            String uri = getJsonField(content, "uri");
            return new OperatingRoomInfo(id, uri);
        });
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
