package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.domain.model.PriorityClass;
import org.example.domain.model.SurgeryEvents;

import java.time.LocalDateTime;

public class MqttSurgeryPhysicalAdapter extends AbstractMqttPhysicalAdapter {

    public final static String PRIORITY_KEY = "priority";
    public final static String STATUS_KEY = "status";
    public final static String LAST_EVENT_KEY = "last_event";
    public final static String PATIENT_ID_KEY = "patient_id";
    public final static String PROGRAMMED_DATE_KEY = "programmed_date";
    public final static String EXECUTION_START_KEY = "execution_start_date";
    public final static String EXECUTION_END_KEY = "execution_end_date";
    public final static String WARNING_KEY = "warning";
    public final static String SURGERY_EVENT_KEY = "updateSurgeryState";
    public final static String SURGERY_INCISION_KEY = "incision_timestamp";
    public final static String SURGERY_SUTURE_KEY = "suture_timestamp";
    public final static String PROGRAMMED_IN_KEY = "programmedIn";
    public final static String EXECUTED_IN_KEY = "executedIn";
    public final static String ADMISSION_TIME_KEY = "admissionTime";
    public final static String ARRIVAL_TIME_KEY = "arrivalTime";

    private String baseTopic = "";
    private final MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttSurgeryPhysicalAdapter(String idDT, String host, Integer port) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        builder.addPhysicalAssetEventAndTopic(SURGERY_EVENT_KEY, "text/plain", "anylogic/id/surgery/" + idDT + "/" + SURGERY_EVENT_KEY, SurgeryEvents::valueOf);
        builder.addPhysicalAssetPropertyAndTopic(PRIORITY_KEY, "", "anylogic/id/surgery/" + idDT + "/" + PRIORITY_KEY, i -> {
            if(i.equals(PriorityClass.A.toString()) || i.equals(PriorityClass.B.toString()) || i.equals(PriorityClass.C.toString()) || i.equals(PriorityClass.D.toString()))
                return i;
            else
                throw new IllegalArgumentException();
        });

        this.baseTopic = "anylogic/id/surgery/" + idDT + "/";

        this.addStringProperty(STATUS_KEY, "");
        this.addStringProperty(LAST_EVENT_KEY, "");
        this.addStringEvent(WARNING_KEY);
        this.addStringEvent(PATIENT_ID_KEY);
        this.addStringEvent(PROGRAMMED_IN_KEY);
        this.addStringEvent(EXECUTED_IN_KEY);

        this.addStringProperty(PROGRAMMED_DATE_KEY, ""); // String.valueOf(LocalDateTime.now()) for test
        this.addStringProperty(EXECUTION_START_KEY, "");
        this.addStringProperty(EXECUTION_END_KEY, "");
        this.addStringProperty(SURGERY_INCISION_KEY, "");
        this.addStringProperty(SURGERY_SUTURE_KEY, "");
        this.addStringProperty(ADMISSION_TIME_KEY, "");
        this.addStringProperty(ARRIVAL_TIME_KEY, "");
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
