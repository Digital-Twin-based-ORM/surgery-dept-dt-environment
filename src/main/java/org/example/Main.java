package org.example;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import org.example.dt.PatientDigitalTwin;
import org.example.dt.SurgeryDigitalTwin;
import org.example.dt.VSMDigitalTwin;
import org.example.dt.property.PatientProperties;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        try{
            DigitalTwin patient = new PatientDigitalTwin("1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getDefault(), new PatientProperties("Tizio Caio", "Sempronio", "M", LocalDate.now())).getDigitalTwin();
            DigitalTwin vsm = new VSMDigitalTwin("1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getDefault()).getDigitalTwin();
            DigitalTwin surgery = new SurgeryDigitalTwin("1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getDefault()).getDigitalTwin();

            // Create the Digital Twin Engine
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            // Directly start when you add it passing a second boolean value = true
            digitalTwinEngine.addDigitalTwin(patient, true);
            digitalTwinEngine.addDigitalTwin(vsm, true);
            digitalTwinEngine.addDigitalTwin(surgery, true);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}