package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttVSMPhysicalAdapter {

    MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttVSMPhysicalAdapter(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);

        builder.addPhysicalAssetPropertyAndTopic("heartRate", 0, "anylogic/id/VitalSignMonitor/" + idDT + "/heartRate", Integer::parseInt);
        builder.addPhysicalAssetPropertyAndTopic("serialCode", 0, "anylogic/id/VitalSignMonitor/" + idDT + "/serialcode", Integer::parseInt);
        builder.addPhysicalAssetPropertyAndTopic("patientId", "", "anylogic/id/VitalSignMonitor/" + idDT + "/patientId", i -> i);
    }

    public MqttVSMPhysicalAdapter(MqttPhysicalAdapterConfigurationBuilder builder) {
        this.builder = builder;
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, builder.build());
    }
}
