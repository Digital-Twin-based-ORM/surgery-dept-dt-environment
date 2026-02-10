package org.example.dt;

import io.github.webbasedwodt.adapter.WoDTDigitalAdapter;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.model.dtd.DTVersion;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.configuration.DigitalRegisterConfiguration;
import org.example.digitalAdapter.custom.RegisterDigitalAdapter;
import org.example.digitalAdapter.custom.SurgeryKpiDigitalAdapter;
import org.example.digitalAdapter.mqtt.SurgeryMqttDigitalAdapter;
import org.example.digitalAdapter.configuration.SurgeryDepConfiguration;
import org.example.dt.property.SurgeryProperties;
import org.example.physicalAdapter.MqttSurgeryPhysicalAdapter;

import org.example.semantics.MedicalDeviceSemantic;
import org.example.semantics.SurgerySemantic;
import org.example.shadowing.SurgeryShadowingFunction;
import org.example.utils.GlobalValues;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.net.URI;
import java.util.Set;

import static org.example.utils.GlobalValues.REGISTER_PLATFORM;
import static org.example.utils.GlobalValues.SURGERY_TYPE;

public class SurgeryDigitalTwin {

    private final DigitalTwin digitalTwin;

    // TODO unire MqttPropertiesConfig e MQTTAdapterConfiguration
    public SurgeryDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, String idDepDT, SurgeryProperties properties, int wodtPortNumber) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException, StorageException, MqttDigitalAdapterConfigurationException {
        // Create the new Digital Twin with its Shadowing Function
        this.digitalTwin = new DigitalTwin(idDT, new SurgeryShadowingFunction(idDT, properties));

        MqttSurgeryPhysicalAdapter builder = new MqttSurgeryPhysicalAdapter(idDT, mqttConfig.getHost(), mqttConfig.getPort(), properties);
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-surgery-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = builder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        SurgeryKpiDigitalAdapter surgeryKpiDigitalAdapter = new SurgeryKpiDigitalAdapter(idDT, new SurgeryDepConfiguration(idDepDT), new MqttPropertiesConfig("127.0.0.1", 1883));
        SurgeryMqttDigitalAdapter surgeryMqttDigitalAdapter = new SurgeryMqttDigitalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDepDT);

        RegisterDigitalAdapter registerDigitalAdapter = new RegisterDigitalAdapter("register-da-" + idDT, new DigitalRegisterConfiguration(REGISTER_PLATFORM, String.valueOf(connectionConfig.getPort()), SURGERY_TYPE, idDT));

        WoDTDigitalAdapter woDTDigitalAdapter = new WoDTDigitalAdapter(
                idDT,
                new WoDTDigitalAdapterConfiguration(
                        URI.create("http://" + GlobalValues.WODT_DT_BASE_HOST + ":" + wodtPortNumber),
                        new DTVersion(1, 0, 0),
                        new SurgerySemantic(),
                        wodtPortNumber,
                        "surgery-" + idDT + "-" + connectionConfig.getPort(),
                        Set.of(URI.create("http://localhost:4000"))
                )
        );

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(surgeryMqttDigitalAdapter.build("-mqtt-da"));
        digitalTwin.addDigitalAdapter(surgeryKpiDigitalAdapter);
        digitalTwin.addDigitalAdapter(woDTDigitalAdapter);
        digitalTwin.addDigitalAdapter(registerDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
