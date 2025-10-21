package org.example.dt;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.exception.*;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.digitalAdapter.sql.KpiRepositoryConfiguration;
import org.example.digitalAdapter.sql.KpiRepositorylDigitalAdapter;
import org.example.infrastructureLayer.persistence.repository.KpiDataSourceGatewayImpl;
import org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter;
import org.example.shadowing.SurgeryDepShadowingFunction;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.sql.SQLException;
import java.util.List;

public class SurgeryDepartmentDigitalTwin {

    private final DigitalTwin digitalTwin;

    public SurgeryDepartmentDigitalTwin(String idDT, MqttPropertiesConfig mqttConfig, HttpConnectionConfig connectionConfig, List<String> operatingRoomsId) throws ModelException, WldtRuntimeException, WldtWorkerException, EventBusException, WldtDigitalTwinStateException, WldtConfigurationException, MqttPhysicalAdapterConfigurationException, MqttException, SQLException {
        this.digitalTwin = new DigitalTwin(idDT, new SurgeryDepShadowingFunction(idDT, operatingRoomsId));

        MqttSurgeryDepPhysicalAdapter builder = new MqttSurgeryDepPhysicalAdapter(idDT, mqttConfig.getHost(), mqttConfig.getPort());
        HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration(idDT + "-surgery-dep-http-adapter", connectionConfig.getHost(), connectionConfig.getPort());

        MqttPhysicalAdapter mqttPhysicalAdapter = builder.build(idDT + "-mqtt-pa");

        HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

        KpiRepositoryConfiguration kpiRepositoryConfiguration = new KpiRepositoryConfiguration("dtName", "dtId", "localhost:6033", "user_name", "root_password", "wldt-db", "surgeries_kpi", "prolonged_turnover_time", "operating_rooms_kpi");
        KpiRepositorylDigitalAdapter sqlDigitalAdapter = new KpiRepositorylDigitalAdapter("mysql-digital-adapter", new KpiDataSourceGatewayImpl(kpiRepositoryConfiguration));

        // Physical Adapter with Configuration
        digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
        digitalTwin.addDigitalAdapter(httpDigitalAdapter);
        digitalTwin.addDigitalAdapter(sqlDigitalAdapter);
    }

    public DigitalTwin getDigitalTwin() {
        return digitalTwin;
    }
}
