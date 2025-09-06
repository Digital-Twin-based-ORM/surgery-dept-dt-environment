package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
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
import org.example.domain.model.SurgeryEventInTime;
import org.example.domain.model.SurgeryEvents;
import org.example.domain.model.Warning;
import org.example.dt.property.InternalProperties;
import org.example.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;
import static org.example.physicalAdapter.MqttSurgeryPhysicalAdapter.*;
import static org.example.utils.GlobalValues.*;

@SuppressWarnings("unchecked")
public class SurgeryShadowingFunction extends AbstractShadowing {
    private static final Logger logger = LoggerFactory.getLogger(SurgeryShadowingFunction.class);

    //TODO each event like a property?
    private final Map<SurgeryEvents, String> eventsTimestamps = new HashMap<>();
    private final List<String> writeOnceProperties = List.of(PROGRAMMED_DATE_KEY, EXECUTION_START_KEY, EXECUTION_END_KEY, SURGERY_INCISION_KEY, SURGERY_SUTURE_KEY);
    private PhysicalAssetRelationship<String> patientRelationship = null;
    private PhysicalAssetRelationship<String> programmedOpRoomRelationship = null;
    private PhysicalAssetRelationship<String> executedOpRoomRelationship = null;
    private String idDT;

    public SurgeryShadowingFunction(String id, InternalProperties properties) {
        super("surgery-" + id + "-shadowing", properties);
        this.idDT = id;
    }
    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected void onCreate() {
        Arrays.stream(SurgeryEvents.values()).forEach(i -> {
            eventsTimestamps.put(i, "");
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

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if (physicalAssetEventWldtEvent != null) {
            String eventKey = physicalAssetEventWldtEvent.getPhysicalEventKey();
            long timestamp = physicalAssetEventWldtEvent.getCreationTimestamp(); // valid for real life operation
            logger.info("Event notified... " + physicalAssetEventWldtEvent);

            // based on the event change the appropriate property: if something strange occurs generate an anomaly (an event not registered for example)
            switch (eventKey) {
                case SURGERY_EVENT_KEY -> {
                    SurgeryEventInTime event = (SurgeryEventInTime) physicalAssetEventWldtEvent.getBody();
                    String eventTimestamp = event.timestamp(); // valid for a simulated physical world
                    long eventTimeStampMillis = LocalDateTime.parse(eventTimestamp).atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli();
                    logger.info("Surgery event notified... " + event);
                    try {
                        checkIfOperationStarted(event.event(), eventTimestamp);
                    } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateEventNotificationException e) {
                        throw new RuntimeException(e);
                    }
                    this.updateSurgeryEvents(event.event(), eventTimestamp);
                    try {
                        super.updateProperty(LAST_EVENT_KEY, event.event().getName());
                        this.updateKPI(eventTimeStampMillis);
                        // notify new event to Dep Twin (using the digital adapter)
                        digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(NEW_SURGERY_EVENT, "", timestamp));
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

    private void updateSurgeryEvents(SurgeryEvents event, String timestamp) {
        Optional<SurgeryEvents> previousEventOpt;
        // TODO maybe adding some policy classes?
        if(event.equals(SurgeryEvents.InOutR)) {
            if(!Objects.equals(eventsTimestamps.get(SurgeryEvents.InR), "")) {
                previousEventOpt = Optional.of(SurgeryEvents.OutR);
            } else {
                previousEventOpt = Optional.of(SurgeryEvents.InR);
            }
        } else if((event.equals(SurgeryEvents.InOutORB))) {
            if(!Objects.equals(eventsTimestamps.get(SurgeryEvents.InORB), "")) {
                previousEventOpt = Optional.of(SurgeryEvents.OutORB);
            } else {
                previousEventOpt = Optional.of(SurgeryEvents.InORB);
            }
        } else  {
            previousEventOpt = event.getPreviousEvent();
        }

//        if(previousEventOpt.isPresent()) {
//            if(eventsTimestamps.get(previousEventOpt.get()) == 0L || eventsTimestamps.get(previousEventOpt.get()) > timestamp) {
//                // TODO notify error
//                logger.error("Inconsistent event update: " + event);
//            }
//        }
        logger.info("The new events set is: " + eventsTimestamps);
        logger.info("Update state to... " + event);
        eventsTimestamps.replace(event, timestamp);
    }

    private void checkIfOperationStarted(SurgeryEvents event, String timestamp) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStateEventNotificationException {
        if(event.equals(SurgeryEvents.StCh)) {
            Optional<DigitalTwinStateProperty<?>> programmedDatePropertyOpt = digitalTwinStateManager.getDigitalTwinState().getProperty(PROGRAMMED_DATE_KEY);
            if(programmedDatePropertyOpt.isPresent()) {
                DigitalTwinStateProperty<String> programmedDateProperty = (DigitalTwinStateProperty<String>) programmedDatePropertyOpt.get();
                String dateTime = programmedDateProperty.getValue();
                logger.info("Programmed date time: " + dateTime);
                LocalDateTime triggerTime = LocalDateTime.parse(timestamp);
                LocalDateTime programmedLDT = LocalDateTime.parse(dateTime);
                logger.info("Trigger time: " + triggerTime + " - Programmed: " + programmedLDT);
                // M10
                long startTimeTardiness = Duration.between(programmedLDT, triggerTime).getSeconds();

                Double minutes = ((double)startTimeTardiness / 60);
                logger.info("Start Time Tardiness: " + minutes);
                System.out.println("STT equals to " + minutes.intValue() + " minutes and " + ((minutes - minutes.intValue()) * 60) + "seconds" );
                // notify event to digital adapter
                digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M10, new Pair<String, String>(this.getId(), "" + startTimeTardiness), triggerTime.atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()));
            }
        }
    }

    private void updateKPI(Long timestamp) throws WldtDigitalTwinStateEventNotificationException {
        try {
            if(eventsTimestamps.get(SurgeryEvents.PzPr) != "") {
                // M15
                LocalDateTime pzPrDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.PzPr));
                LocalDateTime stAnestDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.StAnest));
                logger.info("PzPr: " + pzPrDateTime + " - stAnest: " + stAnestDateTime);
                long tAnest = Duration.between(stAnestDateTime, pzPrDateTime).toSeconds();
                // notify event to digital adapter
                digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M15, new Pair<String, String>(this.getId(), "" + tAnest), timestamp));
                logger.info("T Anest: " + tAnest + " seconds");
            }
            if(!Objects.equals(eventsTimestamps.get(SurgeryEvents.EndCh), "")) {
                // M14
                LocalDateTime endChDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.EndCh));
                LocalDateTime stChDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.StCh));
                long tChir = Duration.between(stChDateTime, endChDateTime).toSeconds();
                // notify event to digital adapter
                digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M14, new Pair<String, String>(this.getId(), "" + tChir), timestamp));
                logger.info("T Chir: " + tChir + " seconds");
                if(!Objects.equals(eventsTimestamps.get(SurgeryEvents.OutSO), "")) {
                    // M17
                    LocalDateTime outSoDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.OutSO));
                    LocalDateTime inSoDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.InSO));
                    LocalDateTime stAnestDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.StAnest));

                    float touchTime = Duration.between(stAnestDateTime, outSoDateTime).toSeconds();
                    // notify event to digital adapter
                    digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M17, new Pair<String, String>(this.getId(), "" + touchTime), timestamp));
                    logger.info("Touch time: " + touchTime + " seconds");
                    // M26
                    long valueAddedTimeDen = Duration.between(inSoDateTime, outSoDateTime).toSeconds();;;
                    float valueAddedTime = (float) tChir / valueAddedTimeDen;
                    logger.info("Value added time: " + valueAddedTime);
                    // notify event to digital adapter
                    digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M26, new Pair<String, String>(this.getId(), "" + valueAddedTime), timestamp));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
