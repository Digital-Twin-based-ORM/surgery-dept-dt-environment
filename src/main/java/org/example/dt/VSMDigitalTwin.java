package org.example.dt;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.custom.VSMDigitalAdapter;
import org.example.digitalAdapter.configuration.VSMConfiguration;
import org.example.physicalAdapter.MqttVSMPhysicalAdapter;
import org.example.shadowing.VSMShadowingFunction;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.util.List;

public class VSMDigitalTwin {

    private final DigitalTwin digitalTwin;

    public VSMDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException {
        digitalTwin = new DigitalTwin("vsm-" + idDT + "-digital-twin", new VSMShadowingFunction("vsm-" + idDT + "-shadowing"));
        MqttVSMPhysicalAdapter builderVSM = new MqttVSMPhysicalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDT);
        MqttPhysicalAdapter mqttPhysicalAdapterVSM = builderVSM.build(idDT + "-vsm-mqtt-pa");

        HttpDigitalAdapterConfiguration configVsm = new HttpDigitalAdapterConfiguration(idDT + "-vsm-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());
        HttpDigitalAdapter httpDigitalAdapterVsm = new HttpDigitalAdapter(configVsm, digitalTwin);

        MqttPropertiesConfig configDigitalAdapter = new MqttPropertiesConfig("127.0.0.1", 1883);
        VSMConfiguration vsmConfiguration = new VSMConfiguration(List.of("heartRate"));

        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapterVSM);
        digitalTwin.addDigitalAdapter(new VSMDigitalAdapter(idDT + "-vsm-digital-adapter", vsmConfiguration, configDigitalAdapter));
        digitalTwin.addDigitalAdapter(httpDigitalAdapterVsm);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
