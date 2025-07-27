package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.example.domain.model.DailySlot;
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

    public OperatingRoomShadowing(String id) {
        super(id);
    }

    public OperatingRoomShadowing(String id, OperatingRoomProperties properties) {
        super(id, properties);
    }

    public OperatingRoomShadowing(String id, OperatingRoomProperties properties, String surgeryDepUri) {
        super(id, properties);
        this.surgeryDepUri = Optional.of(surgeryDepUri);
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
                        super.updateProperty(LAST_DISINFECTION, "" + physicalAssetEventWldtEvent.getCreationTimestamp());
                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case ASSIGN_DAILY_SLOTS -> {
                    // TODO do tests
                    DailySlot slots = (DailySlot) physicalAssetEventWldtEvent.getBody();
                    try {
                        if(digitalTwinStateManager.getDigitalTwinState().getProperty(DAILY_SLOTS).isPresent()) {
                            DigitalTwinStateProperty<?> slotsProperty = digitalTwinStateManager.getDigitalTwinState().getProperty(DAILY_SLOTS).get();
                            Map<String, DailySlot> dailySlots = (Map<String, DailySlot>) slotsProperty.getValue();
                            dailySlots.put(slots.getDay().toString(), slots);
                            super.updateProperty(DAILY_SLOTS, dailySlots);
                        }
                    } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case NOW_AVAILABLE -> {
                    // silently ignored
                }
                case BUSY -> {
                    // silently ignored
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
