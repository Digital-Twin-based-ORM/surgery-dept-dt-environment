package org.example;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import org.example.domain.model.HospitalizationRegime;
import org.example.domain.model.PriorityClass;
import org.example.domain.model.Surgery;
import org.example.domain.model.fhir.CodeableConcept;
import org.example.dt.*;
import org.example.dt.property.OperatingRoomProperties;
import org.example.dt.property.PatientProperties;
import org.example.dt.property.SurgeryProperties;
import org.example.infrastructureLayer.properties.DailySlotsYamlReader;
import org.example.infrastructureLayer.persistence.SurgeryDataAccess;
import org.example.utils.HttpConnectionConfig;
import org.example.utils.MqttPropertiesConfig;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static String DEP_ID = "department1";

    public static void main(String[] args) {
        try{
            // Create the Digital Twin Engine
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            DigitalTwin depDt = new SurgeryDepartmentDigitalTwin(DEP_ID, MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8077), List.of("or_1", "or_2")).getDigitalTwin();
            digitalTwinEngine.addDigitalTwin(depDt, true);

            DigitalTwin patient = new PatientDigitalTwin("patient_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8078), new PatientProperties("Tizio Caio", "Sempronio", "M", LocalDate.now())).getDigitalTwin();

            DigitalTwin vsm = new VSMDigitalTwin("vsm_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8079)).getDigitalTwin();

            ArrayList<Surgery> surgeries = SurgeryDataAccess.retrieveSimulationData(SurgeryDataAccess.getConnection());
            for(int i = 0; i < surgeries.size(); i++ ) {
                Surgery surgery = surgeries.get(i);
                SurgeryProperties properties = new SurgeryProperties(
                        new CodeableConcept("",""),
                        new CodeableConcept("",""),
                        new CodeableConcept("",""),
                        LocalDateTime.now().toString(),
                        surgery.getAdmissionDate().toString(),
                        surgery.getProgrammedDate().toString(),
                        PriorityClass.C,
                        HospitalizationRegime.ORDINARY,
                        surgery.getEstimatedTime()
                );
                DigitalTwin surgeryDT = new SurgeryDigitalTwin("surgery_" + surgery.getIdSurgery(), MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8082 + i), DEP_ID, properties).getDigitalTwin();
                digitalTwinEngine.addDigitalTwin(surgeryDT, true);
            }

            DigitalTwin orDT_1 = new OperatingRoomDigitalTwin("or_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8070), new OperatingRoomProperties("sala1", "or_1", DailySlotsYamlReader.readDailySlots("or_1")), DEP_ID).getDigitalTwin();

            DigitalTwin orDT_2 = new OperatingRoomDigitalTwin("or_2", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8071), new OperatingRoomProperties("sala2", "or_2", DailySlotsYamlReader.readDailySlots("or_1")), DEP_ID).getDigitalTwin();

            // Directly start when you add it passing a second boolean value = true
            digitalTwinEngine.addDigitalTwin(patient, true);
            // digitalTwinEngine.addDigitalTwin(vsm, true);
            digitalTwinEngine.addDigitalTwin(orDT_1, true);
            digitalTwinEngine.addDigitalTwin(orDT_2, true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initSlots() {

    }
}