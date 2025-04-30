package org.example.shadowing;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.state.DigitalTwinStateProperty;
import org.slf4j.Logger;

import java.util.Map;

public abstract class AbstractShadowing extends ShadowingFunction {


    public AbstractShadowing(String id) {
        super(id);
    }

    public abstract Logger getLogger();

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
