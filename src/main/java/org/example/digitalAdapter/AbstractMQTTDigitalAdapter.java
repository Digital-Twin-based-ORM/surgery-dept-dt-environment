package org.example.digitalAdapter;

import it.wldt.adapter.digital.DigitalAdapter;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.example.digitalAdapter.configuration.MQTTAdapterConfiguration;
import org.slf4j.Logger;

public interface AbstractMQTTDigitalAdapter {

    default void publishUpdate(String id, String value, String valueType, String body) {
        System.out.println("Attempting to send message...");
        String topic        = getMQTTConfiguration().getBaseTopic() + "/" + id + "/" + valueType;
        int qos             = 2;
        String broker       = getMQTTConfiguration().getBroker();
        String clientId     = getMQTTConfiguration().getClientId();
        MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: " + value);
            MqttMessage message = new MqttMessage(body.getBytes());
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
            getLogger().error(me.getMessage());
        }
    }

    public Logger getLogger();

    public MQTTAdapterConfiguration getMQTTConfiguration();
}
