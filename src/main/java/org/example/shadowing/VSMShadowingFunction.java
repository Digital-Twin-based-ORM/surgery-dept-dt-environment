package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class VSMShadowingFunction extends ShadowingFunction {

    private static final Logger logger = LoggerFactory.getLogger(VSMShadowingFunction.class);
    public VSMShadowingFunction(String id) {
        super(id);
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
        try{

            // NEW from 0.3.0 -> Start DT State Change Transaction
            this.digitalTwinStateManager.startStateTransaction();

            //Iterate over all the received PAD from connected Physical Adapters
            adaptersPhysicalAssetDescriptionMap.values().forEach(pad -> {
                pad.getProperties().forEach(property -> {
                    try {
                        logger.info("Creo la proprietà: " + property.getKey() + " - " + property.getInitialValue());
                        //Create and write the property on the DT's State
                        this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(property.getKey(),property.getInitialValue()));

                        //Start observing the variation of the physical property in order to receive notifications
                        //Without this call the Shadowing Function will not receive any notifications or callback about
                        //incoming physical property of the target type and with the target key
                        this.observePhysicalAssetProperty(property);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            });

            // NEW from 0.3.0 -> Commit DT State Change Transaction to apply the changes on the DT State and notify about the change
            this.digitalTwinStateManager.commitStateTransaction();

            //Start observation to receive all incoming Digital Action through active Digital Adapter
            //Without this call the Shadowing Function will not receive any notifications or callback about
            //incoming request to execute an exposed DT's Action
            observeDigitalActionEvents();

            //Notify the DT Core that the Bounding phase has been correctly completed and the DT has evaluated its
            //internal status according to what is available and declared through the Physical Adapters
            notifyShadowingSync();

        }catch (Exception e){
            e.printStackTrace();
        }
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
