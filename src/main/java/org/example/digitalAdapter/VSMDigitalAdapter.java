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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VSMDigitalAdapter extends DigitalAdapter<Void> {

    private static final Logger logger = LoggerFactory.getLogger(VSMDigitalAdapter.class);
    public VSMDigitalAdapter(String id, Void configuration) {
        super(id, configuration);
    }

    public VSMDigitalAdapter(String id) {
        super(id);
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {
        // pick the current patient id associated and send the value to it
        logger.info("STATE CHANGES: " + arrayList.size());
        DigitalTwinStateChange stateChange = arrayList.getFirst();
        logger.info("New state update from VSM Digital Adapter: " + arrayList.getFirst());
        logger.info("Current state from VSM Digital Adapter: " + digitalTwinState);
        // DigitalTwinStateChange{operation=OPERATION_UPDATE, resourceType=PROPERTY, resource=DigitalTwinStateProperty{key='heartRate', value=75, type='java.lang.Integer', readable=true, writable=true, exposed=true}}
        // DigitalTwinState{properties={heartRate=DigitalTwinStateProperty{key='heartRate', value=75, type='java.lang.Integer', readable=true, writable=true, exposed=true}
        if(stateChange.getResourceType().equals(DigitalTwinStateChange.ResourceType.PROPERTY) && stateChange.getResource() instanceof DigitalTwinStateProperty<?> stateProperty) {
            try {
                if(digitalTwinState.getPropertyList().isPresent()) {
                    List<DigitalTwinStateProperty<?>> list = digitalTwinState.getPropertyList().get();
                    Optional<DigitalTwinStateProperty<?>> property = list.stream().filter(i -> i.getKey().equals("patiendId")).findFirst();
                    property.ifPresent(digitalTwinStateProperty -> publishUpdate(digitalTwinStateProperty.getValue().toString(), stateProperty.getValue().toString(), stateProperty.getKey()));
                }
            } catch (WldtDigitalTwinStatePropertyException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void publishUpdate(String patientId, String value, String valueType) {
        String topic        = "patient/" + patientId + "/" + valueType;
        int qos             = 2;
        String broker       = "tcp://127.0.0.1:1883";
        String clientId     = "kotlin_mqtt_subscriber_" + System.currentTimeMillis();
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: " + value);
            MqttMessage message = new MqttMessage(value.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");
            sampleClient.disconnect();
            System.out.println("Disconnected");
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            logger.error(me.getMessage());
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
