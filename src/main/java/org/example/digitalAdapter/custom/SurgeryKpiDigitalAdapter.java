package org.example.digitalAdapter.custom;

import com.google.gson.JsonObject;
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.EventBusException;
import org.example.digitalAdapter.configuration.SurgeryDepConfiguration;
import org.example.digitalAdapter.handler.DigitalHandler;
import org.example.utils.MqttPropertiesConfig;
import org.example.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;

@SuppressWarnings("ALL")
public class SurgeryKpiDigitalAdapter extends DigitalAdapter<SurgeryDepConfiguration> implements AbstractMQTTDigitalAdapter {

    public static String SURGERY_CREATED_NOTIFICATION = "surgeryCreatedNotification";
    private static final Logger logger = LoggerFactory.getLogger(SurgeryKpiDigitalAdapter.class);
    private final MqttPropertiesConfig mqttConfiguration;
    private ArrayList<DigitalHandler> handlers = new ArrayList<>();

    public SurgeryKpiDigitalAdapter(String id, SurgeryDepConfiguration configuration, MqttPropertiesConfig mqttConfiguration) {
        super(id, configuration);
        this.mqttConfiguration = mqttConfiguration;
        String baseTopic = "anylogic/id/dep/" + configuration.getIdDigitalTwin() + "/";

        this.handlers.add(new DigitalHandler<>(M10, baseTopic + M10, this::convertKpiToJson));
        this.handlers.add(new DigitalHandler<>(M14, baseTopic + M14, this::convertKpiToJson));
        this.handlers.add(new DigitalHandler<>(M15, baseTopic + M15, this::convertKpiToJson));
        this.handlers.add(new DigitalHandler<>(M17, baseTopic + M17, this::convertKpiToJson));
        this.handlers.add(new DigitalHandler<>(M26, baseTopic + M26, this::convertKpiToJson));
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        logger.info("Event notified inside digital adapter... " + digitalTwinStateEventNotification.getDigitalEventKey());
        String eventKey = digitalTwinStateEventNotification.getDigitalEventKey();
        Object body = digitalTwinStateEventNotification.getBody();
        Optional<DigitalHandler> handler = handlers.stream().filter(i -> i.getKey().equals(eventKey)).findFirst();
        if(handler.isPresent()) {
            Function<Object, String> function = handler.get().getHandler();
            String message = function.apply(body);
            this.publishUpdate(handler.get().getTopic(), message);
        }
    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {
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
    public MqttPropertiesConfig getMQTTConfiguration() {
        return mqttConfiguration;
    }

    private String convertKpiToJson(Pair<String, String> kpi) {
        JsonObject obj = new JsonObject();
        obj.addProperty("surgeryId", kpi.getLeft());
        obj.addProperty("value", kpi.getRight());
        return obj.toString();
    }
}
