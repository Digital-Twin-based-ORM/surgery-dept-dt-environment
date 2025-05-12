package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttAllSurgeriesPhysicalAdapter {

    MqttPhysicalAdapterConfigurationBuilder builder;
    public static String AGGREGATE_DT_ID = "1";

    public MqttAllSurgeriesPhysicalAdapter(String host, Integer port) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);

        this.builder.addPhysicalAssetEventAndTopic("patientCreated", "text/plain", "patients/" + AGGREGATE_DT_ID + "/created", i -> i);
        this.builder.addPhysicalAssetEventAndTopic("patientProgrammed", "text/plain", "patients/" + AGGREGATE_DT_ID + "/programmed", i -> i);
        this.builder.addPhysicalAssetEventAndTopic("patientExecuted", "text/plain", "patients/" + AGGREGATE_DT_ID + "/executed", i -> i);
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, builder.build());
    }
}
