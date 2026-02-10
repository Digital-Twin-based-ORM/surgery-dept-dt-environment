package org.example.digitalAdapter.sql;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.EventBusException;
import org.example.businessLayer.adapter.KpiDigitalRecord;
import org.example.businessLayer.boundaries.KpiDataSourceGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KpiRepositorylDigitalAdapter extends DigitalAdapter<KpiDataSourceGateway> {

    private final Logger logger = LoggerFactory.getLogger(KpiRepositorylDigitalAdapter.class);
    public static final String SURGERY_KPI_UPDATE = "surgeryKpiUpdate";
    public static final String OPERATING_ROOM_KPI_UPDATE = "orKpiUpdate";
    public static final String M22_UPDATE = "m22Update";

    public KpiRepositorylDigitalAdapter(String id, KpiDataSourceGateway configuration) throws SQLException {
        super(id, configuration);
    }

    @Override
    protected void onStateUpdate(DigitalTwinState digitalTwinState, DigitalTwinState digitalTwinState1, ArrayList<DigitalTwinStateChange> arrayList) {

    }

    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
        String eventKey = digitalTwinStateEventNotification.getDigitalEventKey();
        this.logger.info("[MySqlDigitalAdapter] -> onEventNotificationReceived(): " + eventKey);
        KpiDigitalRecord surgeryKpiNotification = (KpiDigitalRecord) digitalTwinStateEventNotification.getBody();
        switch (eventKey) {
            case SURGERY_KPI_UPDATE -> {
                this.getConfiguration().addSurgeryKpiRecord(surgeryKpiNotification.id(), surgeryKpiNotification.type(), surgeryKpiNotification.desc(), surgeryKpiNotification.value(), surgeryKpiNotification.timestamp());
            }
            case OPERATING_ROOM_KPI_UPDATE -> {
                this.getConfiguration().addOperatingRoomKpiRecord(surgeryKpiNotification.id(), surgeryKpiNotification.type(), surgeryKpiNotification.value(), surgeryKpiNotification.timestamp());
            }
            case M22_UPDATE -> {
                this.getConfiguration().addM22Kpi(surgeryKpiNotification.value(), surgeryKpiNotification.timestamp().toLocalDate());
            }
        }
    }

    @Override
    public void onAdapterStart() {
        logger.info("Adapter started...");
    }

    @Override
    public void onAdapterStop() {

    }

    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {
        this.logger.info("[MySqlDigitalAdapter] -> onDigitalTwinSync(): " + digitalTwinState);
        try {
            this.observeDigitalTwinEventsNotifications(List.of(SURGERY_KPI_UPDATE, OPERATING_ROOM_KPI_UPDATE));
        } catch (EventBusException e) {
            logger.error(e.getMessage());
        }
    }

    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onDigitalTwinCreate() {

    }

    @Override
    public void onDigitalTwinStart() {

    }

    @Override
    public void onDigitalTwinStop() {

    }

    @Override
    public void onDigitalTwinDestroy() {

    }
}
