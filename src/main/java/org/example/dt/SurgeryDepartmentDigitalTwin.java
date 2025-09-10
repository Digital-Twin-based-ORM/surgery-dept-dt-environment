package org.example.dt;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter;
import org.example.shadowing.SurgeryDepShadowingFunction;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

public class SurgeryDepartmentDigitalTwin {

    private final DigitalTwin digitalTwin;

    public SurgeryDepartmentDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, WldtConfigurationException, MqttPhysicalAdapterConfigurationException, MqttException {
        this.digitalTwin = new DigitalTwin(idDT, new SurgeryDepShadowingFunction(idDT));

        MqttSurgeryDepPhysicalAdapter builder = new MqttSurgeryDepPhysicalAdapter(idDT, mqttConfig.getHost(), mqttConfig.getPort());
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-surgery-dep-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = builder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
