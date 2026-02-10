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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {

    public static String DEP_ID = "department1";
    public static int DEP_PORT = 8077;
    public static String BASE_URL = "http://localhost";

    static final int BASE_WODT_PORT_DEP = 5000;
    static final int BASE_WODT_PORT_PATIENT = 6000;
    static final int BASE_WODT_PORT_SURGERY = 7000;
    static final int BASE_WODT_PORT_OR = 4500;
    static final int BASE_WODT_PORT_VSM = 9000;

    private static final Random random = new Random();

    public static void main(String[] args) {
        try{
            // Create the Digital Twin Engine
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            DigitalTwin depDt = new SurgeryDepartmentDigitalTwin(DEP_ID, MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8077), List.of("or_1", "or_2"), BASE_WODT_PORT_DEP).getDigitalTwin();
            digitalTwinEngine.addDigitalTwin(depDt, true);

            DigitalTwin patient = new PatientDigitalTwin("patient_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8078), new PatientProperties("Tizio Caio", "Sempronio", "M", LocalDate.now(), "12345"), BASE_WODT_PORT_PATIENT).getDigitalTwin();
            DigitalTwin patient1 = new PatientDigitalTwin("patient_2", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(1000), new PatientProperties("Boida", "De", "M", LocalDate.now(), "12d345"), BASE_WODT_PORT_PATIENT + 1).getDigitalTwin();

            DigitalTwin vsm = new VSMDigitalTwin("vsm_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8079), BASE_WODT_PORT_VSM).getDigitalTwin();

            ArrayList<Surgery> surgeries = SurgeryDataAccess.retrieveSimulationData(SurgeryDataAccess.getConnection());
            // for testing the over threshold KPI
//            surgeries.add(new Surgery(
//                    "surgery_0",
//                    LocalDateTime.now(),
//                    LocalDateTime.now(),
//                    LocalDateTime.now(),
//                    PriorityClass.A,
//                    null,
//                    2,
//                    LocalDateTime.parse("2025-01-01 09:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//            ));
            for(int i = 0; i < surgeries.size(); i++ ) {
                Surgery surgery = surgeries.get(i);
                String idDt = "surgery_" + surgery.getIdSurgery();
                SurgeryProperties properties = new SurgeryProperties(
                        new CodeableConcept("123","Reason surgery"),
                        getRandomSurgeryType(),
                        new CodeableConcept("123","Code surgery"),
                        LocalDateTime.now().toString(),
                        surgery.getAdmissionDate().toString(),
                        surgery.getProgrammedDate().toString(),
                        PriorityClass.C,
                        HospitalizationRegime.ORDINARY, // todo add to db table
                        surgery.getEstimatedTime(),
                        surgery.getArrivalDate().toString(),
                        surgery.getWaitingListInsertionDate().toString(),
                        idDt
                );
                DigitalTwin surgeryDT = new SurgeryDigitalTwin(idDt, MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8082 + i), DEP_ID, properties, BASE_WODT_PORT_SURGERY + i).getDigitalTwin();
                digitalTwinEngine.addDigitalTwin(surgeryDT);
            }

            DigitalTwin orDT_1 = new OperatingRoomDigitalTwin("or_1", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8070), new OperatingRoomProperties("sala1", "or_1", DailySlotsYamlReader.readDailySlots("or_1")), DEP_ID, BASE_WODT_PORT_OR, BASE_URL + ":" + DEP_PORT).getDigitalTwin();

            DigitalTwin orDT_2 = new OperatingRoomDigitalTwin("or_2", MqttPropertiesConfig.getDefault(), HttpConnectionConfig.getWithPort(8071), new OperatingRoomProperties("sala2", "or_2", DailySlotsYamlReader.readDailySlots("or_1")), DEP_ID, BASE_WODT_PORT_OR + 1, BASE_URL + ":" + DEP_PORT).getDigitalTwin();

            // Directly start when you add it passing a second boolean value = true
            digitalTwinEngine.addDigitalTwin(patient);
            digitalTwinEngine.addDigitalTwin(patient1);
            //digitalTwinEngine.addDigitalTwin(vsm, true);
            digitalTwinEngine.addDigitalTwin(orDT_1);
            digitalTwinEngine.addDigitalTwin(orDT_2);

            digitalTwinEngine.startAll();

            digitalTwinEngine.getDigitalTwinMap().entrySet().forEach(i -> {
                System.out.println("DigitalTwin: " + i.getKey() + " - " +  i.getValue().getDigitalAdapterIds());
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static CodeableConcept getRandomSurgeryType() {
        List<CodeableConcept> surgeries = List.of(
                new CodeableConcept("80146002", "Appendicectomia"),
                new CodeableConcept("708781007", "Artroplastica totale dell'anca"),
                new CodeableConcept("23393003", "Bypass coronarico")
        );
        int index = random.nextInt(3);
        return surgeries.get(index);
    }
}