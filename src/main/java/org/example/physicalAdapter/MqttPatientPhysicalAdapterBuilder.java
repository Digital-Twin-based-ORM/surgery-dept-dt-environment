package org.example.physicalAdapter;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.domain.model.SurgeryMetadata;
import org.example.utils.UtilsFunctions;

import static org.example.utils.GlobalValues.PATIENT_TYPE;
import static org.example.utils.GlobalValues.TYPE;

public class MqttPatientPhysicalAdapterBuilder extends AbstractMqttPhysicalAdapter {

    public final static String SURGERY_REQUEST = "newSurgeryRequested";
    private String baseTopic = "";
    MqttPhysicalAdapterConfigurationBuilder builder;
    MqttPhysicalAdapter adapter = null;

    public MqttPatientPhysicalAdapterBuilder(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        // Configuring the mqtt physical and http digital adapter
        this.baseTopic = "anylogic/id/Patient/" + idDT;
        this.addStringProperty("currentLocation", "", baseTopic + "/currentLocation");
        this.addIntProperty("heartRate", 0, "patient/" + idDT + "/heartRate");
        this.builder.addPhysicalAssetEventAndTopic("bpmAnomaly", "text/plain", baseTopic + "/bpmAnomaly", String::toString);
        this.addStringProperty(TYPE, PATIENT_TYPE);
        this.builder.addPhysicalAssetEventAndTopic(SURGERY_REQUEST, "text/plain", baseTopic + "/" + SURGERY_REQUEST, (String i) ->  {
            try {
                System.out.println("JSON value: " + i);
                JsonObject jsonObject = UtilsFunctions.stringToJsonObjectGson(i);
                assert jsonObject != null;
                return new SurgeryMetadata(jsonObject.get("uri").getAsString(), jsonObject.get("id").getAsString());
            } catch (Exception ex) {
                System.out.println("EXCEPTIOON: " + ex.getMessage());
                throw new RuntimeException(ex);
            }

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

    @Override
    public String getBaseTopic() {
        return baseTopic;
    }

    @Override
    public MqttPhysicalAdapterConfigurationBuilder getBuilder() {
        return builder;
    }
}
