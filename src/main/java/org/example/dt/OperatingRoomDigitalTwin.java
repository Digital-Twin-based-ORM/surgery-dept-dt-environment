package org.example.dt;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.digital.MqttDigitalAdapter;
import it.wldt.adapter.mqtt.digital.exception.MqttDigitalAdapterConfigurationException;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.mqtt.OperatingRoomMqttDigitalAdapter;
import org.example.dt.property.OperatingRoomProperties;
import org.example.physicalAdapter.MqttOperatingRoomPhysicalAdapter;
import org.example.shadowing.OperatingRoomShadowing;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

public class OperatingRoomDigitalTwin {

    private final DigitalTwin digitalTwin;

    public OperatingRoomDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, OperatingRoomProperties properties, String idDep) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException, MqttDigitalAdapterConfigurationException {
        this.digitalTwin = new DigitalTwin(idDT, new OperatingRoomShadowing("or-" + idDT + "-shadowing", properties));

        MqttOperatingRoomPhysicalAdapter mqttPhysicalAdapterBuilder = new MqttOperatingRoomPhysicalAdapter(idDT, mqttConfig.getHost(), mqttConfig.getPort());
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-or-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = mqttPhysicalAdapterBuilder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        MqttDigitalAdapter operatingRoomMqttDigitalAdapter = new OperatingRoomMqttDigitalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDep).build("or_" + idDT + "mqtt-da");

        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(operatingRoomMqttDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }

}
