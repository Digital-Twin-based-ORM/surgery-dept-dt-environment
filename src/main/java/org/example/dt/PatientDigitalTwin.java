package org.example.dt;

import io.github.webbasedwodt.adapter.WoDTDigitalAdapter;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.model.dtd.DTVersion;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.configuration.DigitalRegisterConfiguration;
import org.example.digitalAdapter.custom.RegisterDigitalAdapter;
import org.example.digitalAdapter.mqtt.MqttPatientDigitalAdapter;
import org.example.dt.property.PatientProperties;
import org.example.physicalAdapter.MqttPatientPhysicalAdapterBuilder;
import org.example.semantics.PatientSemantic;
import org.example.shadowing.PatientShadowingFunction;
import org.example.utils.GlobalValues;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.net.URI;
import java.util.Set;

import static org.example.utils.GlobalValues.PATIENT_TYPE;
import static org.example.utils.GlobalValues.REGISTER_PLATFORM;

public class PatientDigitalTwin {

    private final DigitalTwin digitalTwin;

    public PatientDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, PatientProperties properties, int wodtPortNumber) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException, MqttDigitalAdapterConfigurationException {
        // Create the new Digital Twin with its Shadowing Function

        this.digitalTwin = new DigitalTwin(idDT, new PatientShadowingFunction("patient-" + idDT + "-shadowing", properties));

        MqttPatientPhysicalAdapterBuilder builder = new MqttPatientPhysicalAdapterBuilder(mqttConfig.getHost(), mqttConfig.getPort(), idDT);
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-patient-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = builder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        MqttPatientDigitalAdapter builderDigitalAdapter = new MqttPatientDigitalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDT);
        MqttDigitalAdapter mqttDigitalAdapter = builderDigitalAdapter.build(idDT + "-mqtt-da");

        RegisterDigitalAdapter registerDigitalAdapter = new RegisterDigitalAdapter("register-da-" + idDT, new DigitalRegisterConfiguration(REGISTER_PLATFORM, String.valueOf(connectionConfig.getPort()), PATIENT_TYPE, idDT));

        WoDTDigitalAdapter woDTDigitalAdapter = new WoDTDigitalAdapter(
                "patient-dt-adapter",
                new WoDTDigitalAdapterConfiguration(
                        URI.create("http://" + GlobalValues.WODT_DT_BASE_HOST + ":" + wodtPortNumber),
                        new DTVersion(1, 0, 0),
                        new PatientSemantic(),
                        wodtPortNumber,
                        "patient-" + idDT + "-" + connectionConfig.getPort(),
                        Set.of(URI.create("http://localhost:4000"))
                )
        );

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(mqttDigitalAdapter);
        digitalTwin.addDigitalAdapter(woDTDigitalAdapter);
        digitalTwin.addDigitalAdapter(registerDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
