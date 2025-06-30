package org.example.physicalAdapter;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.utils.UtilsFunctions;

public class MqttPatientPhysicalAdapterBuilder {

    public final static String SURGERY_REQUEST = "newSurgeryRequested";
    MqttPhysicalAdapterConfigurationBuilder builder;
    MqttPhysicalAdapter adapter = null;

    public MqttPatientPhysicalAdapterBuilder(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        // TODO DT storage
        // Configuring the mqtt physical and http digital adapter
        this.addStringProperty("currentLocation", "", "anylogic/id/Patient/" + idDT + "/currentLocation");
        this.addIntProperty("heartRate", 0, "patient/" + idDT + "/heartRate");
        this.builder.addPhysicalAssetEventAndTopic("bpmAnomaly", "text/plain", "anylogic/id/Patient/" + idDT + "/bpmAnomaly", String::toString);
        this.builder.addPhysicalAssetEventAndTopic(SURGERY_REQUEST, "text/plain", "anylogic/id/Patient/" + idDT + "/" + SURGERY_REQUEST, (String i) ->  {
            JsonObject jsonObject = UtilsFunctions.stringToJsonObjectGson(i);
            assert jsonObject != null;
            return jsonObject.get("uri").getAsString();
        });
    }

    void addStringProperty(String key, String initialValue, String topic) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        builder.addPhysicalAssetPropertyAndTopic(key, initialValue, topic, i -> i);
    }

    void addIntProperty(String key, Integer initialValue, String topic) throws MqttPhysicalAdapterConfigurationException {
        // Configuring the mqtt physical and http digital adapter
        builder.addPhysicalAssetPropertyAndTopic(key, initialValue, topic, Integer::parseInt);
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        adapter = new MqttPhysicalAdapter(id, builder.build());
        return adapter;
    }

}
