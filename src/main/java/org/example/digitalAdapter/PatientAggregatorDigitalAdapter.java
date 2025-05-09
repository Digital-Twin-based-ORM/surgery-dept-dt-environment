package org.example.digitalAdapter;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import org.example.digitalAdapter.configuration.MQTTAdapterConfiguration;
import org.example.digitalAdapter.configuration.PatientsAggregatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class PatientAggregatorDigitalAdapter extends DigitalAdapter<PatientsAggregatorConfiguration> implements AbstractMQTTDigitalAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PatientAggregatorDigitalAdapter.class);
    private final MQTTAdapterConfiguration mqttConfiguration;

    public PatientAggregatorDigitalAdapter(String id, PatientsAggregatorConfiguration configuration, MQTTAdapterConfiguration mqttConfiguration) {
        super(id, configuration);
        this.mqttConfiguration = mqttConfiguration;
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {
        // TODO hello from patient
    }

    @Override
    public void onDigitalTwinStop() {
        // TODO notification that the patient has concluded his surgery journey
    }

    @Override
    public void onDigitalTwinDestroy() {

    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public MQTTAdapterConfiguration getMQTTConfiguration() {
        return mqttConfiguration;
    }

    @Override
    public void publishUpdate(String id, String value, String valueType, String body) {
        AbstractMQTTDigitalAdapter.super.publishUpdate(id, value, valueType, body);
    }
}
