package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.example.digitalAdapter.mqtt.MqttPatientDigitalAdapter;
import org.example.domain.model.HealthInformation;
import org.example.domain.model.SurgeryMetadata;
import org.example.dt.property.PatientProperties;
import org.example.physicalAdapter.MqttPatientPhysicalAdapterBuilder;
import org.example.utils.UtilsFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static org.example.digitalAdapter.mqtt.MqttPatientDigitalAdapter.NEW_HEART_RATE;
import static org.example.utils.GlobalValues.*;

public class PatientShadowingFunction extends AbstractShadowing {

    private static final Logger logger = LoggerFactory.getLogger(PatientShadowingFunction.class);
    private HealthInformation healthInformation = new HealthInformation();
    private String lastCurrentlyLocatedRelationshipInstanceKey = "";

    PhysicalAssetRelationship<String> subjectedToRelationship = null;
    PhysicalAssetRelationship<String> currentlyLocatedRelationship = null;

    public PatientShadowingFunction(String id) {
        super(id);
    }

    public PatientShadowingFunction(String id, PatientProperties immutableProperties) {
        super(id, immutableProperties);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        //Create an empty PAD
        PhysicalAssetDescription pad = new PhysicalAssetDescription();

        //Create Test Relationship to describe that the Physical Device is inside a building
        this.subjectedToRelationship = new PhysicalAssetRelationship<>(SURGERY_RELATIONSHIP_NAME, SURGERY_RELATIONSHIP_TYPE);
        this.currentlyLocatedRelationship = new PhysicalAssetRelationship<>(LOCATED_IN_RELATIONSHIP_NAME, LOCATED_IN_RELATIONSHIP_TYPE);
        pad.getRelationships().add(subjectedToRelationship);
        pad.getRelationships().add(currentlyLocatedRelationship);

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
        super.onPhysicalAssetPropertyVariation(physicalAssetPropertyWldtEvent);
        switch (physicalAssetPropertyWldtEvent.getPhysicalPropertyId()) {
            case "heartRate": {
                int value = (Integer) physicalAssetPropertyWldtEvent.getBody();
                healthInformation.addRegisteredBPM(physicalAssetPropertyWldtEvent.getCreationTimestamp(), value);
                try {
                    checkPatientHealth();
                    this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(NEW_HEART_RATE, value, UtilsFunctions.getCurrentTimestamp()));
                } catch (EventBusException | WldtDigitalTwinStateEventNotificationException e) {
                    throw new RuntimeException(e);
                }
            }
            case "currentLocation": {
                try {
                    logger.info("New relationship... currentlyLocated");
                    Map<String, Object> relationshipMetadata = new HashMap<>();
                    relationshipMetadata.put("location", physicalAssetPropertyWldtEvent.getBody());

                    if(!lastCurrentlyLocatedRelationshipInstanceKey.isEmpty()) {
                        //this.digitalTwinStateManager.deleteRelationshipInstance(LOCATED_IN_RELATIONSHIP_NAME, lastCurrentlyLocatedRelationshipInstanceKey); //it updates automatically
                    }
                    PhysicalAssetRelationshipInstance<String> relInstance = this.currentlyLocatedRelationship.createRelationshipInstance("emptyUri");
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(relInstance.getRelationship().getName(), relInstance.getTargetId(), relInstance.getKey(), relationshipMetadata));
                    this.digitalTwinStateManager.commitStateTransaction();
                    this.lastCurrentlyLocatedRelationshipInstanceKey = relInstance.getKey();
                } catch (WldtDigitalTwinStateException e) {
                    logger.error("ERROOOOR: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if(Objects.equals(physicalAssetEventWldtEvent.getPhysicalEventKey(), MqttPatientPhysicalAdapterBuilder.SURGERY_REQUEST)) {
            try {
                this.digitalTwinStateManager.startStateTransaction();
                SurgeryMetadata surgery = (SurgeryMetadata)physicalAssetEventWldtEvent.getBody();
                System.out.println("Content: " + surgery.id());
                Map<String, Object> relationshipMetadata = new HashMap<>();
                relationshipMetadata.put("uri", surgery.uri());
                relationshipMetadata.put("id", surgery.id());

                PhysicalAssetRelationshipInstance<String> relInstance = this.subjectedToRelationship.createRelationshipInstance(surgery.id());
                this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(relInstance.getRelationship().getName(), relInstance.getTargetId(), relInstance.getKey(), relationshipMetadata));
                this.digitalTwinStateManager.commitStateTransaction();
            } catch (WldtDigitalTwinStateException e) {
                logger.error("ERROR PATIENT: " + e.getMessage());
                throw new RuntimeException(e);
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
        try {
            String actionKey = digitalActionWldtEvent.getActionKey();
            logger.info("New event: " + actionKey);
            this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPatientHealth() throws EventBusException, WldtDigitalTwinStateEventNotificationException {
        List<Integer> bpms = healthInformation.getLastValues();
        int sum = 0;
        for(int i : bpms) {
            sum += i;
        }
        double mean = (double) sum / (long) bpms.size();
        if(mean < 50 || mean > 110) {
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(MqttPatientDigitalAdapter.BPM_ANOMALY, null, LocalDate.now().toEpochDay()));
        }
    }
}
