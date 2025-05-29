package org.example.digitalAdapter;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.EventBusException;
import org.example.digitalAdapter.configuration.MQTTAdapterConfiguration;
import org.example.digitalAdapter.configuration.SurgeriesAggregatorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class SurgeriesAggregatorDigitalAdapter extends DigitalAdapter<SurgeriesAggregatorConfiguration> implements AbstractMQTTDigitalAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SurgeriesAggregatorDigitalAdapter.class);
    private final MQTTAdapterConfiguration mqttConfiguration;

    public SurgeriesAggregatorDigitalAdapter(String id, SurgeriesAggregatorConfiguration configuration, MQTTAdapterConfiguration mqttConfiguration) {
        super(id, configuration);
        this.mqttConfiguration = mqttConfiguration;
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {
        logger.info("State updated... ");
    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("Event notified inside digital adapter... " + digitalTwinStateEventNotification.getDigitalEventKey());
    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {
        /*
        try {
            Optional<DigitalTwinStateProperty<?>> idDtProperty = digitalTwinState.getProperty("id");
            String  idDT = (String)idDtProperty.orElseThrow().getValue();
        } catch (WldtDigitalTwinStatePropertyException e) {
            throw new RuntimeException(e);
        }
        publishUpdate(getConfiguration().getIdDigitalTwin(), "created", "");
        */
        System.out.println("[DemoDigitalAdapter] -> onDigitalTwinSync(): " + digitalTwinState);

        try {
            digitalTwinState.getEventList()
                    .map(eventList -> eventList.stream()
                            .map(DigitalTwinStateEvent::getKey)
                            .collect(Collectors.toList()))
                    .ifPresent(eventKeys -> {
                        try {
                            this.observeDigitalTwinEventsNotifications(eventKeys);
                        } catch (EventBusException e) {
                            e.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
