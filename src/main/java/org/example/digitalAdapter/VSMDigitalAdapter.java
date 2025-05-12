package org.example.digitalAdapter;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.example.digitalAdapter.configuration.MQTTAdapterConfiguration;
import org.example.digitalAdapter.configuration.VSMConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VSMDigitalAdapter extends DigitalAdapter<VSMConfiguration> implements AbstractMQTTDigitalAdapter {

    private static final Logger logger = LoggerFactory.getLogger(VSMDigitalAdapter.class);
    private final MQTTAdapterConfiguration mqttConfiguration;

    private final String PATIENT_ID_PROPERTY = "patiendId";

    public VSMDigitalAdapter(String id, VSMConfiguration configuration, MQTTAdapterConfiguration mqttConfiguration) {
        super(id, configuration);
        this.mqttConfiguration = mqttConfiguration;
    }

    @Override
    public void publishUpdate(String id, String valueType, String body) {
        AbstractMQTTDigitalAdapter.super.publishUpdate(id, valueType, body);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public MQTTAdapterConfiguration getMQTTConfiguration() {
        return this.mqttConfiguration;
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {
        logger.info("STATE CHANGES: " + arrayList.size());
        List<DigitalTwinStateProperty<?>> changedProperties = arrayList.stream()
                .filter(i -> i.getResource() instanceof DigitalTwinStateProperty<?>)
                .map(i -> (DigitalTwinStateProperty<?>)i.getResource())
                .filter(i -> getConfiguration().getObservedProperties().contains(i.getKey()))
                .collect(Collectors.toList());

        logger.info("New state update from VSM Digital Adapter: " + arrayList.getFirst());
        logger.info("Current state from VSM Digital Adapter: " + digitalTwinState);
        for(DigitalTwinStateProperty<?> property : changedProperties) {
            try {
                if(digitalTwinState.getPropertyList().isPresent()) {
                    List<DigitalTwinStateProperty<?>> list = digitalTwinState.getPropertyList().get();
                    Optional<DigitalTwinStateProperty<?>> patientId = list.stream().filter(i -> i.getKey().equals(PATIENT_ID_PROPERTY)).filter(i -> i.getValue() != "").findFirst();
                    logger.info("Notifying new state...");
                    patientId.ifPresent(digitalTwinStateProperty -> publishUpdate(digitalTwinStateProperty.getValue().toString(), property.getKey(), property.getValue().toString()));
                }
            } catch (WldtDigitalTwinStatePropertyException e) {
                throw new RuntimeException(e);
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
