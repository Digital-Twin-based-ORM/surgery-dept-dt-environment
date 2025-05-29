package org.example.shadowing;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.DigitalTwinStateAction;
import it.wldt.core.state.DigitalTwinStateEvent;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStateException;
import org.example.dt.property.InternalProperties;
import org.slf4j.Logger;

import java.util.Map;

public abstract class AbstractShadowing extends ShadowingFunction {

    private final InternalProperties properties;

    public AbstractShadowing(String id) {
        super(id);
        properties = new InternalProperties();
    }

    public AbstractShadowing(String id, InternalProperties properties) {
        super(id);
        this.properties = properties;
    }

    public abstract Logger getLogger();

    @Override
    protected void onStart() {
        // create static initial values of the DT, these are not linked to any physical adapter
        for(PhysicalAssetProperty<?> property: properties.getProperties()) {
            try {
                this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(property.getKey(), property.getInitialValue()));
            } catch (WldtDigitalTwinStateException e) {
                throw new RuntimeException(e);
            }
        }
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
                        getLogger().info("Creo la proprietà: " + property.getKey() + " - " + property.getInitialValue());
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

                //Iterate over available declared Physical Events for the target Physical Adapter's PAD
                pad.getEvents().forEach(event -> {
                    try {

                        //Instantiate a new DT State Event with the same key and type
                        DigitalTwinStateEvent dtStateEvent = new DigitalTwinStateEvent(event.getKey(), event.getType());

                        //Create and write the event on the DT's State
                        this.digitalTwinStateManager.registerEvent(dtStateEvent);

                        //Start observing the variation of the physical event in order to receive notifications
                        //Without this call the Shadowing Function will not receive any notifications or callback about
                        //incoming physical events of the target type and with the target key
                        this.observePhysicalAssetEvent(event);

                        System.out.println("[TestShadowingFunction] -> onDigitalTwinBound() -> Event Created & Observed:" + event.getKey());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                pad.getActions().forEach(action -> {
                    try {

                        //Instantiate a new DT State Action with the same key and type
                        DigitalTwinStateAction dtStateAction = new DigitalTwinStateAction(action.getKey(), action.getType(), action.getContentType());

                        //Enable the action on the DT's State
                        this.digitalTwinStateManager.enableAction(dtStateAction);

                        System.out.println("[TestShadowingFunction] -> onDigitalTwinBound() -> Action Enabled:" + action.getKey());

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
}
