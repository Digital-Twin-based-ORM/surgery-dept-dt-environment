package org.example.physicalAdapter;

import com.google.gson.JsonObject;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.domain.model.fhir.DeviceStatus;
import org.example.domain.model.fhir.SurgeryStatus;
import org.example.utils.UtilsFunctions;

public class MqttVSMPhysicalAdapter {

    public final static String SET_PATIENT = "setPatient";
    public final static String HEART_RATE = "heartRate";
    public final static String SERIAL_CODE = "serialCode";
    public final static String DEVICE_STATUS = "status";
    public final static String VSM_PATIENT_ID = "patientId";

    MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttVSMPhysicalAdapter(String host, Integer port, String idDT) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);

        builder.addPhysicalAssetPropertyAndTopic(HEART_RATE, 0, "anylogic/id/VitalSignMonitor/" + idDT + "/" + HEART_RATE, Integer::parseInt);
        builder.addPhysicalAssetPropertyAndTopic(DEVICE_STATUS, DeviceStatus.INACTIVE, "anylogic/id/VitalSignMonitor/" + idDT + "/" + DEVICE_STATUS, i -> {
            if(DeviceStatus.isValid(i)) {
                return DeviceStatus.valueOf(i);
            } else {
                return DeviceStatus.ENTERED_IN_ERROR;
            }
        });
        builder.addPhysicalAssetPropertyAndTopic(SERIAL_CODE, 0, "anylogic/id/VitalSignMonitor/" + idDT + "/" + SERIAL_CODE, Integer::parseInt);
        builder.addPhysicalAssetPropertyAndTopic(VSM_PATIENT_ID, "", "anylogic/id/VitalSignMonitor/" + idDT + "/" + VSM_PATIENT_ID, i -> i);
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
