package org.example.physicalAdapter;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.utils.UtilsFunctions;

public class MqttVSMPhysicalAdapter {

    public final static String SET_PATIENT = "setPatient";

    MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttVSMPhysicalAdapter(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);

        builder.addPhysicalAssetPropertyAndTopic("heartRate", 0, "anylogic/id/VitalSignMonitor/" + idDT + "/heartRate", Integer::parseInt);
        builder.addPhysicalAssetPropertyAndTopic("serialCode", 0, "anylogic/id/VitalSignMonitor/" + idDT + "/serialcode", Integer::parseInt);
        builder.addPhysicalAssetPropertyAndTopic("patientId", "", "anylogic/id/VitalSignMonitor/" + idDT + "/patientId", i -> i);
        this.builder.addPhysicalAssetEventAndTopic(SET_PATIENT, "text/plain", "anylogic/id/VitalSignMonitor/" + idDT + "/" + SET_PATIENT, (String i) ->  {
            JsonObject jsonObject = UtilsFunctions.stringToJsonObjectGson(i);
            assert jsonObject != null;
            return jsonObject.get("uri").getAsString();
        });
    }

    public MqttVSMPhysicalAdapter(MqttPhysicalAdapterConfigurationBuilder builder) {
        this.builder = builder;
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, builder.build());
    }
}
