package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.apache.commons.lang3.tuple.Pair;
import org.example.domain.model.SurgeryEvents;
import org.example.domain.model.Warning;
import org.example.dt.property.InternalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static org.example.physicalAdapter.MqttSurgeryPhysicalAdapter.*;
import static org.example.utils.GlobalValues.*;

public class SurgeryShadowingFunction extends AbstractShadowing{
    private static final Logger logger = LoggerFactory.getLogger(SurgeryShadowingFunction.class);
    private final Map<SurgeryEvents, Long> eventsTimestamps = new HashMap<>();
    private final List<String> writeOnceProperties = List.of(PROGRAMMED_DATE_KEY, EXECUTION_START_KEY, EXECUTION_END_KEY, SURGERY_INCISION_KEY, SURGERY_SUTURE_KEY);
    private PhysicalAssetRelationship<String> patientRelationship = null;
    private PhysicalAssetRelationship<String> programmedOpRoomRelationship = null;
    private PhysicalAssetRelationship<String> executedOpRoomRelationship = null;

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
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        PhysicalAssetDescription pad = new PhysicalAssetDescription();
        pad.getProperties().add(new PhysicalAssetProperty<>("score", 0));
        pad.getProperties().add(new PhysicalAssetProperty<>("warnings", new ArrayList<Warning>()));

        this.patientRelationship = new PhysicalAssetRelationship<>(PATIENT_OPERATED_RELATIONSHIP_NAME, PATIENT_OPERATED_RELATIONSHIP_TYPE);
        this.programmedOpRoomRelationship = new PhysicalAssetRelationship<>(PROGRAMMED_IN_RELATIONSHIP_NAME, PROGRAMMED_IN_RELATIONSHIP_TYPE);
        this.executedOpRoomRelationship = new PhysicalAssetRelationship<>(EXECUTED_IN_RELATIONSHIP_NAME, EXECUTED_IN_RELATIONSHIP_TYPE);
        pad.getRelationships().add(patientRelationship);
        pad.getRelationships().add(programmedOpRoomRelationship);
        pad.getRelationships().add(executedOpRoomRelationship);

        adaptersPhysicalAssetDescriptionMap.put("relationship_pad", pad);
        super.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
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
            String id = physicalAssetPropertyWldtEvent.getPhysicalPropertyId();

            // check write once properties
            if(writeOnceProperties.contains(id)) {
                DigitalTwinStateProperty<?> oldValue = digitalTwinStateManager.getDigitalTwinState().getProperty(id).get();
                if(!Objects.equals(oldValue.getValue().toString(), "")) {
                    return;
                }
            }

            //Update Digital Twin State
            //NEW from 0.3.0 -> Start State Transaction
            this.digitalTwinStateManager.startStateTransaction();

            this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(id, physicalAssetPropertyWldtEvent.getBody()));

            //NEW from 0.3.0 -> Commit State Transaction
            this.digitalTwinStateManager.commitStateTransaction();

        } catch (WldtDigitalTwinStateException e) {
            e.printStackTrace();
        } catch (WldtDigitalTwinStatePropertyException e) {
            throw new RuntimeException(e);
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
                case SURGERY_EVENT_KEY -> {
                    SurgeryEvents event = (SurgeryEvents) physicalAssetEventWldtEvent.getBody();
                    this.updateSurgeryEvents(event, timestamp);
                    try {
                        super.updateProperty(LAST_EVENT_KEY, event.getName());
                        digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(physicalAssetEventWldtEvent.getPhysicalEventKey(), "", LocalDate.now().toEpochDay()));
                    } catch (WldtDigitalTwinStateException | WldtDigitalTwinStateEventNotificationException e) {
                        throw new RuntimeException(e);
                    }
                }
                case WARNING_KEY -> {
                    try {
                        if(digitalTwinStateManager.getDigitalTwinState().getProperty("warnings").isPresent()) {
                            DigitalTwinStateProperty<?> warningsProperty = digitalTwinStateManager.getDigitalTwinState().getProperty("warnings").get();
                            ArrayList<Warning> warnings = (ArrayList<Warning>) warningsProperty.getValue();
                            warnings.add(new Warning(timestamp, (String)physicalAssetEventWldtEvent.getBody()));
                            super.updateProperty("warnings", warnings);
                        }
                    } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case PATIENT_ID_KEY -> {
                    try {
                        this.digitalTwinStateManager.startStateTransaction();
                        String patientUri = (String)physicalAssetEventWldtEvent.getBody();

                        this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(PATIENT_OPERATED_RELATIONSHIP_NAME, patientUri, "patient"));
                        this.digitalTwinStateManager.commitStateTransaction();
                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case PROGRAMMED_IN_KEY -> {
                    try {
                        this.digitalTwinStateManager.startStateTransaction();
                        String operatingRoomUri = (String)physicalAssetEventWldtEvent.getBody();

                        this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(PROGRAMMED_IN_RELATIONSHIP_NAME, operatingRoomUri, "programmedOR"));
                        this.digitalTwinStateManager.commitStateTransaction();
                    } catch (WldtDigitalTwinStateException e) {
                        throw new RuntimeException(e);
                    }
                }
                case EXECUTED_IN_KEY -> {
                    try {
                        this.digitalTwinStateManager.startStateTransaction();
                        String operatingRoomUri = (String)physicalAssetEventWldtEvent.getBody();

                        this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(EXECUTED_IN_RELATIONSHIP_NAME, operatingRoomUri, "executedOR"));
                        this.digitalTwinStateManager.commitStateTransaction();
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

    private void updateSurgeryEvents(SurgeryEvents event, Long timestamp) {
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
        logger.info("Update state to... " + event);
    }
}
