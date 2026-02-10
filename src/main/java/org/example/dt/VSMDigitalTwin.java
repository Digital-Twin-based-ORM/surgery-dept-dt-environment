package org.example.dt;

import io.github.webbasedwodt.adapter.WoDTDigitalAdapter;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.model.dtd.DTVersion;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.configuration.DigitalRegisterConfiguration;
import org.example.digitalAdapter.custom.RegisterDigitalAdapter;
import org.example.digitalAdapter.custom.VSMDigitalAdapter;
import org.example.digitalAdapter.configuration.VSMConfiguration;
import org.example.physicalAdapter.MqttVSMPhysicalAdapter;
import org.example.semantics.MedicalDeviceSemantic;
import org.example.semantics.PatientSemantic;
import org.example.shadowing.VSMShadowingFunction;
import org.example.utils.GlobalValues;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.example.utils.GlobalValues.REGISTER_PLATFORM;
import static org.example.utils.GlobalValues.VSM_TYPE;

public class VSMDigitalTwin {

    private final DigitalTwin digitalTwin;

    public VSMDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, int wodtPortNumber) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException {
        digitalTwin = new DigitalTwin("vsm-" + idDT + "-digital-twin", new VSMShadowingFunction("vsm-" + idDT + "-shadowing"));
        MqttVSMPhysicalAdapter builderVSM = new MqttVSMPhysicalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDT);
        MqttPhysicalAdapter mqttPhysicalAdapterVSM = builderVSM.build(idDT + "-vsm-mqtt-pa");

        HttpDigitalAdapterConfiguration configVsm = new HttpDigitalAdapterConfiguration(idDT + "-vsm-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());
        HttpDigitalAdapter httpDigitalAdapterVsm = new HttpDigitalAdapter(configVsm, digitalTwin);

        MqttPropertiesConfig configDigitalAdapter = new MqttPropertiesConfig("127.0.0.1", 1883);
        VSMConfiguration vsmConfiguration = new VSMConfiguration(List.of("heartRate"));

        RegisterDigitalAdapter registerDigitalAdapter = new RegisterDigitalAdapter("register-da-" + idDT, new DigitalRegisterConfiguration(REGISTER_PLATFORM, String.valueOf(connectionConfig.getPort()), VSM_TYPE, idDT));

        WoDTDigitalAdapter woDTDigitalAdapter = new WoDTDigitalAdapter(
                idDT,
                new WoDTDigitalAdapterConfiguration(
                        URI.create("http://" + GlobalValues.WODT_DT_BASE_HOST + ":" + wodtPortNumber),
                        new DTVersion(1, 0, 0),
                        new MedicalDeviceSemantic(),
                        wodtPortNumber,
                        "vital_signs_monitor-" + idDT + "-" + connectionConfig.getPort(),
                        Set.of(URI.create("http://localhost:4000"))
                )
        );

        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapterVSM);
        digitalTwin.addDigitalAdapter(new VSMDigitalAdapter(idDT + "-vsm-digital-adapter", vsmConfiguration, configDigitalAdapter));
        digitalTwin.addDigitalAdapter(httpDigitalAdapterVsm);
        digitalTwin.addDigitalAdapter(woDTDigitalAdapter);
        digitalTwin.addDigitalAdapter(registerDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
