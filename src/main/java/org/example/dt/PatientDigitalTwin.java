package org.example.dt;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.MqttPatientDigitalAdapter;
import org.example.dt.property.PatientProperties;
import org.example.physicalAdapter.MqttPatientPhysicalAdapterBuilder;
import org.example.shadowing.PatientShadowingFunction;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

public class PatientDigitalTwin {

    private final DigitalTwin digitalTwin;

    public PatientDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, PatientProperties properties) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException, MqttDigitalAdapterConfigurationException {
        // Create the new Digital Twin with its Shadowing Function

        this.digitalTwin = new DigitalTwin(idDT, new PatientShadowingFunction("patient-" + idDT + "-shadowing", properties));

        MqttPatientPhysicalAdapterBuilder builder = new MqttPatientPhysicalAdapterBuilder(mqttConfig.getHost(), mqttConfig.getPort(), idDT);
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-patient-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = builder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        MqttPatientDigitalAdapter builderDigitalAdapter = new MqttPatientDigitalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDT);
        MqttDigitalAdapter mqttDigitalAdapter = builderDigitalAdapter.build(idDT + "-mqtt-pa");

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(mqttDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
