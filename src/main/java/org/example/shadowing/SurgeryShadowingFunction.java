package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetEvent;
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
import org.example.businessLayer.adapter.OperatingRoomInfo;
import org.example.businessLayer.adapter.SurgeryKpiNotification;
import org.example.businessLayer.adapter.SurgeryPropertyChanged;
import org.example.domain.model.*;
import org.example.dt.policy.WriteOnceProperties;
import org.example.dt.property.InternalProperties;
import org.example.dt.property.SurgeryProperties;
import org.example.utils.Pair;
import org.example.utils.UtilsFunctions;
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
    private final WriteOnceProperties writeOnceProperties = new WriteOnceProperties();
    private PhysicalAssetRelationship<String> patientRelationship = null;
    private PhysicalAssetRelationship<String> programmedOpRoomRelationship = null;
    private PhysicalAssetRelationship<String> executedOpRoomRelationship = null;
    private String idDT;
    private SurgeryProperties properties;

    public SurgeryShadowingFunction(String id, SurgeryProperties properties) {
        super("surgery-" + id + "-shadowing", properties);
        this.idDT = id;
        this.properties = (SurgeryProperties) properties;
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
        pad.getEvents().add(new PhysicalAssetEvent(M10, "text/plain"));
        pad.getEvents().add(new PhysicalAssetEvent(M14, "text/plain"));
        pad.getEvents().add(new PhysicalAssetEvent(M15, "text/plain"));
        pad.getEvents().add(new PhysicalAssetEvent(M17, "text/plain"));
        pad.getEvents().add(new PhysicalAssetEvent(M26, "text/plain"));

        this.writeOnceProperties.addKeys(EXECUTION_START_KEY, EXECUTION_END_KEY, SURGERY_INCISION_KEY, SURGERY_SUTURE_KEY);
        Optional<PhysicalAssetProperty<?>> isCancelledProperty = adaptersPhysicalAssetDescriptionMap.get(idDT + "-mqtt-pa").getProperties().stream().filter(i -> i.getKey().equals(IS_CANCELLED)).findFirst();
        isCancelledProperty.ifPresent(physicalAssetProperty -> this.writeOnceProperties.addCustomPolicy(IS_CANCELLED, (Boolean i) -> !i, (Boolean) physicalAssetProperty.getInitialValue()));

        this.patientRelationship = new PhysicalAssetRelationship<>(PATIENT_OPERATED_RELATIONSHIP_NAME, PATIENT_OPERATED_RELATIONSHIP_TYPE);
        this.programmedOpRoomRelationship = new PhysicalAssetRelationship<>(PROGRAMMED_IN_RELATIONSHIP_NAME, PROGRAMMED_IN_RELATIONSHIP_TYPE);
        this.executedOpRoomRelationship = new PhysicalAssetRelationship<>(EXECUTED_IN_RELATIONSHIP_NAME, EXECUTED_IN_RELATIONSHIP_TYPE);
        pad.getRelationships().add(patientRelationship);
        pad.getRelationships().add(programmedOpRoomRelationship);
        pad.getRelationships().add(executedOpRoomRelationship);

        adaptersPhysicalAssetDescriptionMap.put("relationship_pad", pad);
        super.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);

        // send the surgery DT creation notification (to the department for example)
        this.notifySurgeryCreation();
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String s) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String s, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {
        logger.info("Property variation detected... " + physicalAssetPropertyWldtEvent.getPhysicalPropertyId() + " with value " + physicalAssetPropertyWldtEvent.getBody());
        try {
            String id = physicalAssetPropertyWldtEvent.getPhysicalPropertyId();

            try {
                // check write once properties
                if(writeOnceProperties.isModified(id) || !writeOnceProperties.isModificationAllowed(id)) {
                    return;
                }
                this.writeOnceProperties.setModified(id);
                this.writeOnceProperties.setLastValueForCustomPolicy(id, physicalAssetPropertyWldtEvent.getBody());
            } catch (Exception e) {
                logger.error(e.toString());
            }


            //Update Digital Twin State
            //NEW from 0.3.0 -> Start State Transaction
            this.digitalTwinStateManager.startStateTransaction();

            this.digitalTwinStateManager.updateProperty(new DigitalTwinStateProperty<>(id, physicalAssetPropertyWldtEvent.getBody()));

            //NEW from 0.3.0 -> Commit State Transaction
            this.digitalTwinStateManager.commitStateTransaction();

            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(id, new SurgeryPropertyChanged<>(idDT, physicalAssetPropertyWldtEvent.getBody()), physicalAssetPropertyWldtEvent.getCreationTimestamp()));

        } catch (WldtDigitalTwinStateException e) {
            e.printStackTrace();
        } catch (WldtDigitalTwinStateEventNotificationException e) {
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
                    logger.info("Surgery event notified... " + event);
                    logger.info("Update state to... " + event);
                    eventsTimestamps.replace(event.event(), eventTimestamp);
                    try {
                        super.updateProperty(LAST_EVENT_KEY, event.event().getName());
                        // notify new event to Dep Twin (using the digital adapter)
                        digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(SURGERY_EVENT_KEY, event, timestamp));
                    } catch (WldtDigitalTwinStateException | WldtDigitalTwinStateEventNotificationException e) {
                        throw new RuntimeException(e);
                    }
                }
                case SURGERY_TERMINATED -> {
                    try {
                        Long eventTimeStampMillis = LocalDateTime.parse((String)physicalAssetEventWldtEvent.getBody()).atZone(ZoneId.systemDefault())
                                .toInstant()
                                .toEpochMilli();
                        this.updateKPI(eventTimeStampMillis);
                        this.updateProperty(IS_DONE_KEY, true);
                    } catch (Exception e) {
                        logger.error("ERROR surgery: " + e.getMessage());
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
                        this.digitalTwinStateManager.addRelationshipInstance(
                                new DigitalTwinStateRelationshipInstance<>(
                                        PATIENT_OPERATED_RELATIONSHIP_NAME,
                                        patientUri,
                                        "patient"
                                )
                        );
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
                    logger.info("SURGERY EXECUTED ID");
                    try {
                        this.digitalTwinStateManager.startStateTransaction();
                        OperatingRoomInfo operatingRoom = (OperatingRoomInfo)physicalAssetEventWldtEvent.getBody();
                        this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(EXECUTED_IN_RELATIONSHIP_NAME, operatingRoom.uri(), "executedOR"));
                        this.digitalTwinStateManager.commitStateTransaction();
                        digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(EXECUTED_IN_KEY, new SurgeryLocation(this.idDT, operatingRoom.id()), timestamp));
                    } catch (WldtDigitalTwinStateException | WldtDigitalTwinStateEventNotificationException e) {
                        logger.error("ERROR Surgery: " + e.getMessage());
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

    private void calculateTardiness(String timestamp) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStateEventNotificationException {
        Optional<DigitalTwinStateProperty<?>> programmedDatePropertyOpt = digitalTwinStateManager.getDigitalTwinState().getProperty(PROGRAMMED_DATE);
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
            digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M10, new SurgeryKpiNotification(this.idDT, startTimeTardiness,  this.properties.getCategory().getDescription()), triggerTime.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()));
        } else {
            logger.error("Error surgery: programmed date not present");
        }
    }

    private void updateKPI(Long timestamp) throws WldtDigitalTwinStateEventNotificationException {
        try {
            if(eventsTimestamps.get(SurgeryEvents.StCh) != "") {
                calculateTardiness(eventsTimestamps.get(SurgeryEvents.StCh));
            }

            if(eventsTimestamps.get(SurgeryEvents.PzPr) != "") {
                // M15
                LocalDateTime pzPrDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.PzPr));
                LocalDateTime stAnestDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.StAnest));
                logger.info("PzPr: " + pzPrDateTime + " - stAnest: " + stAnestDateTime);
                long tAnest = Duration.between(stAnestDateTime, pzPrDateTime).toSeconds();
                // notify event to digital adapter
                digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M15, new SurgeryKpiNotification(this.idDT, tAnest, this.properties.getCategory().getDescription()), timestamp));
                logger.info("T Anest: " + tAnest + " seconds");
            }
            if(!Objects.equals(eventsTimestamps.get(SurgeryEvents.EndCh), "")) {
                // M14
                LocalDateTime endChDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.EndCh));
                LocalDateTime stChDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.StCh));
                long tChir = Duration.between(stChDateTime, endChDateTime).toSeconds();
                // notify event to digital adapter
                digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M14, new SurgeryKpiNotification(this.idDT, tChir, this.properties.getCategory().getDescription()), timestamp));
                logger.info("T Chir: " + tChir + " seconds");
                if(!Objects.equals(eventsTimestamps.get(SurgeryEvents.OutSO), "")) {
                    // M17
                    LocalDateTime outSoDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.OutSO));
                    LocalDateTime inSoDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.InSO));
                    LocalDateTime stAnestDateTime = LocalDateTime.parse(eventsTimestamps.get(SurgeryEvents.StAnest));

                    float touchTime = Duration.between(stAnestDateTime, outSoDateTime).toSeconds();
                    // notify event to digital adapter
                    digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M17, new SurgeryKpiNotification(idDT, touchTime, this.properties.getCategory().getDescription()), timestamp));
                    logger.info("Touch time: " + touchTime + " seconds");
                    // M26
                    long valueAddedTimeDen = Duration.between(inSoDateTime, outSoDateTime).toSeconds();
                    float valueAddedTime = (float) tChir / valueAddedTimeDen;
                    logger.info("Value added time: " + valueAddedTime);
                    // notify event to digital adapter
                    digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(M26, new SurgeryKpiNotification(idDT, valueAddedTime, this.properties.getCategory().getDescription()), timestamp));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notifySurgeryCreation() {
        // notify surgery dt creation
        SurgeryProperties properties = (SurgeryProperties)super.getProperties();
        try {
            digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(SURGERY_CREATED_NOTIFICATION, new Surgery(
                    this.idDT,
                    properties.getArrivalDate(),
                    properties.getLocalProgrammedDate(),
                    properties.getAdmissionDate(),
                    properties.getPriority(),
                    properties.getRegime(),
                    properties.getEstimatedTime(),
                    properties.getWaitingListInsertionDate()
            ), UtilsFunctions.getCurrentTimestamp()));
        } catch (Exception e) {
            logger.error("Error sending creation notification: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
