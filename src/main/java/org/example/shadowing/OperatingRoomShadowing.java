package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.example.businessLayer.adapter.OperatingRoomDailySlot;
import org.example.domain.model.DailySlot;
import org.example.domain.model.OperatingRoomState;
import org.example.dt.property.OperatingRoomProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static org.example.dt.property.OperatingRoomProperties.DAILY_SLOTS;
import static org.example.physicalAdapter.MqttOperatingRoomPhysicalAdapter.*;
import static org.example.utils.GlobalValues.BELONGS_TO_NAME;
import static org.example.utils.GlobalValues.BELONGS_TO_TYPE;

public class OperatingRoomShadowing extends AbstractShadowing {

    private static final Logger logger = LoggerFactory.getLogger(OperatingRoomShadowing.class);
    private Optional<String> surgeryDepUri = Optional.empty();
    private final String idDT;

    public OperatingRoomShadowing(String id) {
        super(id);
        this.idDT = id;
    }

    public OperatingRoomShadowing(String id, OperatingRoomProperties properties) {
        super(id, properties);
        this.idDT = properties.getIdRoom();
    }

    public OperatingRoomShadowing(String id, OperatingRoomProperties properties, String surgeryDepUri) {
        super(id, properties);
        this.surgeryDepUri = Optional.of(surgeryDepUri);
        this.idDT = properties.getIdRoom();
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String s) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String s, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        PhysicalAssetDescription pad = new PhysicalAssetDescription();
        pad.getRelationships().add(new PhysicalAssetRelationship<>(BELONGS_TO_NAME, BELONGS_TO_TYPE));
        adaptersPhysicalAssetDescriptionMap.put("relationship_pad", pad);

        super.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);

        if(surgeryDepUri.isPresent()) {
            try {
                this.digitalTwinStateManager.startStateTransaction();
                String operatingRoomUri = surgeryDepUri.get();

                this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(BELONGS_TO_NAME, operatingRoomUri, "surgeryDepartment"));
                this.digitalTwinStateManager.commitStateTransaction();
            } catch (WldtDigitalTwinStateException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if (physicalAssetEventWldtEvent != null) {
            String eventKey = physicalAssetEventWldtEvent.getPhysicalEventKey();
            long timestamp = physicalAssetEventWldtEvent.getCreationTimestamp();
            logger.info("Event notified... " + eventKey);

            // based on the event change the appropriate property: if something strange occurs generate an anomaly (an event not registered for example)
            switch (eventKey) {
                case DISINFECTION_TERMINATED -> {
                    try {
                        super.updateProperty(LAST_DISINFECTION, "" + timestamp);
                        this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(STATE, OperatingRoomState.READY_TO_USE.toString()));

                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case ASSIGN_DAILY_SLOTS -> {
                    // invia gli slot del giorno corrente al DT del dipartimento
                    try {
                        if(digitalTwinStateManager.getDigitalTwinState().getProperty(DAILY_SLOTS).isPresent()) {
                            logger.info("FOUND PROPERTY...");
                            DigitalTwinStateProperty<?> slotsProperty = digitalTwinStateManager.getDigitalTwinState().getProperty(DAILY_SLOTS).get();
                            Map<String, DailySlot> dailySlots = (Map<String, DailySlot>) slotsProperty.getValue();
                            String key = (String)physicalAssetEventWldtEvent.getBody();
                            logger.info("KEY: " + key);
                            DailySlot currentDaySlot = dailySlots.get(key);
                            if(currentDaySlot != null) {
                                logger.info("ASSIGNING DAILY SLOTS...");
                                this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(ASSIGN_DAILY_SLOTS, new OperatingRoomDailySlot(this.idDT, currentDaySlot), timestamp));
                            }
                        }
                    } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateEventNotificationException e) {
                        throw new RuntimeException(e);
                    }
                }
                case ADD_NEW_SLOT -> {
                    DailySlot slots = (DailySlot) physicalAssetEventWldtEvent.getBody();
                    try {
                        if(digitalTwinStateManager.getDigitalTwinState().getProperty(DAILY_SLOTS).isPresent()) {
                            DigitalTwinStateProperty<?> slotsProperty = digitalTwinStateManager.getDigitalTwinState().getProperty(DAILY_SLOTS).get();
                            Map<String, DailySlot> dailySlots = (Map<String, DailySlot>) slotsProperty.getValue();
                            dailySlots.put(slots.getLocalDateDay().toString(), slots);
                            super.updateProperty(DAILY_SLOTS, dailySlots);
                        }
                    } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case DISINFECTION_STARTED -> {
                    try {
                        this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(STATE, OperatingRoomState.DISINFECTING.toString()));
                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case NOW_AVAILABLE -> {
                    try {
                        this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(STATE, OperatingRoomState.AVAILABLE.toString()));
                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case BUSY -> {
                    try {
                        this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(STATE, OperatingRoomState.BUSY.toString()));
                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipInstanceDeletedWldtEvent) {

    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {

    }
}
