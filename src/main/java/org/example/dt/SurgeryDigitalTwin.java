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
import org.example.digitalAdapter.SurgeryDigitalAdapter;
import org.example.digitalAdapter.SurgeryMqttDigitalAdapter;
import org.example.digitalAdapter.configuration.MQTTAdapterConfiguration;
import org.example.digitalAdapter.configuration.SurgeryDepConfiguration;
import org.example.dt.property.SurgeryProperties;
import org.example.physicalAdapter.MqttSurgeryPhysicalAdapter;

import org.example.shadowing.SurgeryShadowingFunction;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.time.LocalDateTime;

public class SurgeryDigitalTwin {

    private final DigitalTwin digitalTwin;

    // TODO unire MqttPropertiesConfig e MQTTAdapterConfiguration
    public SurgeryDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, String idDepDT, SurgeryProperties properties) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException, StorageException, MqttDigitalAdapterConfigurationException {
        // Create the new Digital Twin with its Shadowing Function
        this.digitalTwin = new DigitalTwin(idDT, new SurgeryShadowingFunction(idDT, properties));

        MqttSurgeryPhysicalAdapter builder = new MqttSurgeryPhysicalAdapter(idDT, mqttConfig.getHost(), mqttConfig.getPort());
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-surgery-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = builder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        MqttDigitalAdapter mqttDigitalAdapter = new SurgeryMqttDigitalAdapter(mqttConfig.getHost(),mqttConfig.getPort(), idDT, idDepDT).build(idDT + "-mqtt-da");

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(mqttDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
