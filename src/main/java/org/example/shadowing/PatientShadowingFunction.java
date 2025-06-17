package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.EventBusException;
import org.example.domain.model.HealthInformation;
import org.example.dt.property.PatientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.example.dt.PatientDigitalTwin.SUBJECTED_TO;
import static org.example.utils.GlobalValues.SURGERY_RELATIONSHIP_NAME;
import static org.example.utils.GlobalValues.SURGERY_RELATIONSHIP_TYPE;

public class PatientShadowingFunction extends AbstractShadowing {

    private static final Logger logger = LoggerFactory.getLogger(PatientShadowingFunction.class);
    private Timer healthCheck = new Timer("HealthCheck");
    private HealthInformation healthInformation = new HealthInformation();

    PhysicalAssetRelationship<String> subjectedToRelationship = null;

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
        pad.getRelationships().add(subjectedToRelationship);

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
                    this.publishPhysicalAssetActionWldtEvent("newHeartRate", value);
                    checkPatientHealth();
                } catch (EventBusException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {

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

            if(Objects.equals(actionKey, SUBJECTED_TO)) {
                this.digitalTwinStateManager.startStateTransaction();
                String surgeryUri = (String)digitalActionWldtEvent.getBody();
                // TODO add surgery date
                Map<String, Object> relationshipMetadata = new HashMap<>();
                relationshipMetadata.put("surgery_date", "f0");

                PhysicalAssetRelationshipInstance<String> relInstance = this.subjectedToRelationship.createRelationshipInstance(surgeryUri, relationshipMetadata);

                this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(relInstance.getRelationship().getName(), relInstance.getTargetId(), relInstance.getKey()));

                this.digitalTwinStateManager.commitStateTransaction();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPatientHealth() throws EventBusException {
        List<Integer> bpms = healthInformation.getLastValues();
        int sum = 0;
        for(int i : bpms) {
            sum += i;
        }
        double mean = (double) sum / (long) bpms.size();
        if(mean < 50 || mean > 110) {
            this.publishPhysicalAssetActionWldtEvent("bpmAnomaly", mean);
        }
    }
}
