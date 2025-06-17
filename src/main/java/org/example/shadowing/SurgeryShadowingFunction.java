package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.example.domain.model.SurgeryEvents;
import org.example.dt.property.InternalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.example.physicalAdapter.MqttSurgeryPhysicalAdapter.LAST_EVENT;

public class SurgeryShadowingFunction extends AbstractShadowing{

    private static final Logger logger = LoggerFactory.getLogger(SurgeryShadowingFunction.class);
    private final Map<SurgeryEvents, Long> eventsTimestamps = new HashMap<>();
    public SurgeryShadowingFunction(String id, InternalProperties properties) {
        super(id, properties);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected void onCreate() {
        Arrays.stream(SurgeryEvents.values()).forEach(i -> {
            eventsTimestamps.put(i, 0L);
        });
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
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {
        logger.info("Property variation detected... " + physicalAssetPropertyWldtEvent.getPhysicalPropertyId());
        try {
            //Update Digital Twin State
            //NEW from 0.3.0 -> Start State Transaction
            this.digitalTwinStateManager.startStateTransaction();

            this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(physicalAssetPropertyWldtEvent.getPhysicalPropertyId(), physicalAssetPropertyWldtEvent.getBody()));

            //NEW from 0.3.0 -> Commit State Transaction
            this.digitalTwinStateManager.commitStateTransaction();

        } catch (WldtDigitalTwinStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if (physicalAssetEventWldtEvent != null) {
            logger.info("Event notified... " + physicalAssetEventWldtEvent.getPhysicalEventKey());
            // based on the event change the appropriate property: if something strange occurs generate an anomaly (an event not registered for example)
            SurgeryEvents event = (SurgeryEvents) physicalAssetEventWldtEvent.getBody();
            try {
                long timestamp = physicalAssetEventWldtEvent.getCreationTimestamp();
                Optional<SurgeryEvents> previousEventOpt;
                if(event.equals(SurgeryEvents.InOutR)) {
                    if(eventsTimestamps.get(SurgeryEvents.InR) != 0L) {
                        previousEventOpt = Optional.of(SurgeryEvents.OutR);
                    } else {
                        previousEventOpt = Optional.of(SurgeryEvents.InR);
                    }
                } else if((event.equals(SurgeryEvents.InOutORB))) {
                    if(eventsTimestamps.get(SurgeryEvents.InORB) != 0L) {
                        previousEventOpt = Optional.of(SurgeryEvents.OutORB);
                    } else {
                        previousEventOpt = Optional.of(SurgeryEvents.InORB);
                    }
                } else  {
                    previousEventOpt = event.getPreviousEvent();
                }

                if(previousEventOpt.isPresent()) {
                    if(eventsTimestamps.get(previousEventOpt.get()) == 0L || eventsTimestamps.get(previousEventOpt.get()) > timestamp) {
                        // TODO notify error
                        logger.error("Inconsistent event update: " + event);
                    }
                }
                eventsTimestamps.replace(event, timestamp);
                logger.info("The new events set is: " + eventsTimestamps);
                super.updateProperty(LAST_EVENT, event.getName());
                digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(physicalAssetEventWldtEvent.getPhysicalEventKey(), "", LocalDate.now().toEpochDay()));
            } catch (WldtDigitalTwinStateEventNotificationException e) {
                throw new RuntimeException(e);
            } catch (WldtDigitalTwinStateException e) {
                throw new RuntimeException(e);
            }
            logger.info("Update state to... " + event);
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
