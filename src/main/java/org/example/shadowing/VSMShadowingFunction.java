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
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.example.physicalAdapter.MqttVSMPhysicalAdapter.SET_PATIENT;
import static org.example.utils.GlobalValues.*;
import static org.example.utils.GlobalValues.LOCATED_IN_RELATIONSHIP_TYPE;

public class VSMShadowingFunction extends AbstractShadowing {

    private static final Logger logger = LoggerFactory.getLogger(VSMShadowingFunction.class);
    PhysicalAssetRelationship<String> patientRelationship = null;
    public VSMShadowingFunction(String id) {
        super(id);
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
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String s) {
        //Create an empty PAD
        PhysicalAssetDescription pad = new PhysicalAssetDescription();

        //Create Test Relationship to describe that the Physical Device is inside a building
        this.patientRelationship = new PhysicalAssetRelationship<>(PATIENT_MONITORED_RELATIONSHIP_NAME, PATIENT_MONITORED_RELATIONSHIP_TYPE);
        pad.getRelationships().add(patientRelationship);

        adaptersPhysicalAssetDescriptionMap.put("relationship_pad", pad);

        super.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
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
            this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(SET_PATIENT, (String)physicalAssetPropertyWldtEvent.getBody(), physicalAssetPropertyWldtEvent.getCreationTimestamp()));
            //NEW from 0.3.0 -> Commit State Transaction
            this.digitalTwinStateManager.commitStateTransaction();

        } catch (WldtDigitalTwinStateException | WldtDigitalTwinStateEventNotificationException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if(Objects.equals(physicalAssetEventWldtEvent.getPhysicalEventKey(), SET_PATIENT)) {
            try {
                this.digitalTwinStateManager.startStateTransaction();
                String patientUri = (String)physicalAssetEventWldtEvent.getBody();

                Map<String, Object> relationshipMetadata = new HashMap<>();
                relationshipMetadata.put("uri", patientUri);
                PhysicalAssetRelationshipInstance<String> relInstance = this.patientRelationship.createRelationshipInstance(PATIENT_MONITORED_RELATIONSHIP_NAME, relationshipMetadata);
                this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(relInstance.getRelationship().getName(), relInstance.getTargetId(), relInstance.getKey(), relationshipMetadata));
                this.digitalTwinStateManager.commitStateTransaction();
            } catch (WldtDigitalTwinStateException e) {
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

    }
}
