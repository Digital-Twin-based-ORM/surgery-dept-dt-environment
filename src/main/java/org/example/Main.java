package org.example;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import org.example.domain.model.Surgery;
import org.example.dt.PatientDigitalTwin;
import org.example.dt.SurgeryDigitalTwin;
import org.example.dt.VSMDigitalTwin;
import org.example.dt.property.PatientProperties;
import org.example.repositoryMySql.SurgeryDataAccess;
import org.example.repositoryMySql.SurgeryDtDataAccess;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.time.LocalDate;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        try{
            // Create the Digital Twin Engine
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            DigitalTwin patient = new PatientDigitalTwin("patient_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getDefault(), new PatientProperties("Tizio Caio", "Sempronio", "M", LocalDate.now())).getDigitalTwin();

            DigitalTwin vsm = new VSMDigitalTwin("vsm_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8079)).getDigitalTwin();

            ArrayList<Surgery> surgeries = SurgeryDataAccess.retrieveSimulationData(SurgeryDataAccess.getConnection());
            for(int i = 0; i < surgeries.size(); i++ ) {
                // TODO initialize mutable properties
                Surgery surgery = surgeries.get(i);
                DigitalTwin surgeryDT = new SurgeryDigitalTwin("surgery_" + surgery.getIdSurgery(), MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8082 + i)).getDigitalTwin();
                digitalTwinEngine.addDigitalTwin(surgeryDT, true);
            }

            // Directly start when you add it passing a second boolean value = true
            digitalTwinEngine.addDigitalTwin(patient, true);
            digitalTwinEngine.addDigitalTwin(vsm, true);

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}