package org.example;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import org.example.dt.PatientDigitalTwin;
import org.example.dt.VSMDigitalTwin;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

public class Main {
    public static void main(String[] args) {
        try{

            DigitalTwin patient = new PatientDigitalTwin("1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getDefault()).getDigitalTwin();
            DigitalTwin vsm = new VSMDigitalTwin("1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getDefault()).getDigitalTwin();

            // Create the Digital Twin Engine
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            // Add the Digital Twin to the Engine
            //digitalTwinEngine.addDigitalTwin(digitalTwin);

            // Directly start when you add it passing a second boolean value = true
            digitalTwinEngine.addDigitalTwin(patient, true);
            digitalTwinEngine.addDigitalTwin(vsm, true);

            // Starting the single DT on the engine through its id
            // digitalTwinEngine.startDigitalTwin(id);

            // Start all the DTs registered on the engine
            //digitalTwinEngine.startAll();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}