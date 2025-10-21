package org.example.digitalAdapter.custom;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.example.digitalAdapter.configuration.VSMConfiguration;
import org.example.utils.MqttPropertiesConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.example.physicalAdapter.MqttVSMPhysicalAdapter.VSM_PATIENT_ID;

public class VSMDigitalAdapter extends DigitalAdapter<VSMConfiguration> implements AbstractMQTTDigitalAdapter {

    private static final Logger logger = LoggerFactory.getLogger(VSMDigitalAdapter.class);
    private final MqttPropertiesConfig mqttConfiguration;

    public VSMDigitalAdapter(String id, VSMConfiguration configuration, MqttPropertiesConfig mqttConfiguration) {
        super(id, configuration);
        this.mqttConfiguration = mqttConfiguration;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public MqttPropertiesConfig getMQTTConfiguration() {
        return this.mqttConfiguration;
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {
        List<DigitalTwinStateProperty<?>> changedProperties = arrayList.stream()
                .filter(i -> i.getResourceType() == DigitalTwinStateChange.ResourceType.PROPERTY)
                .map(i -> (DigitalTwinStateProperty<?>)i.getResource())
                .filter(i -> getConfiguration().getObservedProperties().contains(i.getKey()))
                .collect(Collectors.toList());

        for(DigitalTwinStateProperty<?> property : changedProperties) {
            try {
                if(digitalTwinState.getPropertyList().isPresent()) {
                    List<DigitalTwinStateProperty<?>> list = digitalTwinState.getPropertyList().get();
                    Optional<DigitalTwinStateProperty<?>> patientId = list.stream().filter(i -> i.getKey().equals(VSM_PATIENT_ID)).filter(i -> i.getValue() != "").findFirst();
                    patientId.ifPresent(digitalTwinStateProperty -> publishUpdate("anylogic/id/Patient/" + digitalTwinStateProperty.getValue().toString() + "/" + property.getKey(), property.getValue().toString()));
                }
            } catch (WldtDigitalTwinStatePropertyException e) {
                logger.error("Error vsm digital adapter: " + e.getMessage());
            }
        }
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

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }
}
