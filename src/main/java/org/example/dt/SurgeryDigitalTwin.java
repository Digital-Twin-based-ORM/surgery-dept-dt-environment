package org.example.dt;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.custom.SurgeryKpiDigitalAdapter;
import org.example.digitalAdapter.mqtt.SurgeryMqttDigitalAdapter;
import org.example.digitalAdapter.configuration.SurgeryDepConfiguration;
import org.example.dt.property.SurgeryProperties;
import org.example.physicalAdapter.MqttSurgeryPhysicalAdapter;

import org.example.shadowing.SurgeryShadowingFunction;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

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

        SurgeryKpiDigitalAdapter surgeryKpiDigitalAdapter = new SurgeryKpiDigitalAdapter(idDT, new SurgeryDepConfiguration(idDepDT), new MqttPropertiesConfig("127.0.0.1", 1883));
        SurgeryMqttDigitalAdapter surgeryMqttDigitalAdapter = new SurgeryMqttDigitalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDT, idDepDT);

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(surgeryMqttDigitalAdapter.build("-mqtt-da"));
        digitalTwin.addDigitalAdapter(surgeryKpiDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
