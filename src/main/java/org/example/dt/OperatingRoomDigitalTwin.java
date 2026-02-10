package org.example.dt;

import io.github.webbasedwodt.adapter.WoDTDigitalAdapter;
import io.github.webbasedwodt.adapter.WoDTDigitalAdapterConfiguration;
import io.github.webbasedwodt.model.dtd.DTVersion;
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
import org.example.digitalAdapter.configuration.DigitalRegisterConfiguration;
import org.example.digitalAdapter.custom.RegisterDigitalAdapter;
import org.example.digitalAdapter.mqtt.OperatingRoomMqttDigitalAdapter;
import org.example.dt.property.OperatingRoomProperties;
import org.example.physicalAdapter.MqttOperatingRoomPhysicalAdapter;
import org.example.semantics.MedicalDeviceSemantic;
import org.example.semantics.OperatingRoomSemantic;
import org.example.shadowing.OperatingRoomShadowing;
import org.example.utils.GlobalValues;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.net.URI;
import java.util.Set;

import static org.example.utils.GlobalValues.OPERATING_ROOM_TYPE;
import static org.example.utils.GlobalValues.REGISTER_PLATFORM;

public class OperatingRoomDigitalTwin {

    private final DigitalTwin digitalTwin;

    public OperatingRoomDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, OperatingRoomProperties properties, String idDep, int wodtPortNumber, String departmentUri) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, MqttPhysicalAdapterConfigurationException, MqttException, WldtConfigurationException, MqttDigitalAdapterConfigurationException {
        this.digitalTwin = new DigitalTwin(idDT, new OperatingRoomShadowing("or-" + idDT + "-shadowing", properties, departmentUri));

        MqttOperatingRoomPhysicalAdapter mqttPhysicalAdapterBuilder = new MqttOperatingRoomPhysicalAdapter(idDT, mqttConfig.getHost(), mqttConfig.getPort());
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-or-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = mqttPhysicalAdapterBuilder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        MqttDigitalAdapter operatingRoomMqttDigitalAdapter = new OperatingRoomMqttDigitalAdapter(mqttConfig.getHost(), mqttConfig.getPort(), idDep).build("or_" + idDT + "mqtt-da");

        RegisterDigitalAdapter registerDigitalAdapter = new RegisterDigitalAdapter("register-da-" + idDT, new DigitalRegisterConfiguration(REGISTER_PLATFORM, String.valueOf(connectionConfig.getPort()), OPERATING_ROOM_TYPE, idDT));

        WoDTDigitalAdapter woDTDigitalAdapter = new WoDTDigitalAdapter(
                idDT,
                new WoDTDigitalAdapterConfiguration(
                        URI.create("http://" + GlobalValues.WODT_DT_BASE_HOST + ":" + wodtPortNumber),
                        new DTVersion(1, 0, 0),
                        new OperatingRoomSemantic(),
                        wodtPortNumber,
                        "operating_room-" + idDT + "-" + connectionConfig.getPort(),
                        Set.of(URI.create("http://localhost:4000"))
                )
        );

        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(operatingRoomMqttDigitalAdapter);
        digitalTwin.addDigitalAdapter(woDTDigitalAdapter);
        digitalTwin.addDigitalAdapter(registerDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }

}
