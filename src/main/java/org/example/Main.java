package org.example;

import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapter;
import it.wldt.adapter.http.digital.adapter.HttpDigitalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinEngine;
import org.example.digitalAdapter.VSMDigitalAdapter;
import org.example.physicalAdapter.MqttPatientPhysicalAdapter;
import org.example.physicalAdapter.MqttVSMPhysicalAdapter;
import org.example.shadowing.PatientShadowingFunction;
import org.example.shadowing.VSMShadowingFunction;

public class Main {
    public static void main(String[] args) {
        try{
            String id = "test-digital-twin";

            /** PATIENT DT CREATION */
            // Create the new Digital Twin with its Shadowing Function
            DigitalTwin digitalTwin = new DigitalTwin(id, new PatientShadowingFunction("test-shadowing"));

            // MqttPhysicalAdapterConfigurationBuilder builder = MqttPhysicalAdapterConfiguration.builder("127.0.0.1", 1883);
            MqttPatientPhysicalAdapter builder = new MqttPatientPhysicalAdapter("127.0.0.1", 1883, "1");
            HttpDigitalAdapterConfiguration config = new HttpDigitalAdapterConfiguration("my-http-adapter", "localhost", 8080);

            MqttPhysicalAdapter mqttPhysicalAdapter = builder.build("test-mqtt-pa");

            HttpDigitalAdapter httpDigitalAdapter = new HttpDigitalAdapter(config, digitalTwin);

            // Physical Adapter with Configuration
            digitalTwin.addPhysicalAdapter(mqttPhysicalAdapter);
            digitalTwin.addDigitalAdapter(httpDigitalAdapter);

            /** VSM DT CREATION */
            DigitalTwin digitalTwinVSM = new DigitalTwin("vsm-s1-digital-twin", new VSMShadowingFunction("vsm-shadowing"));
            MqttVSMPhysicalAdapter builderVSM = new MqttVSMPhysicalAdapter("127.0.0.1", 1883, "1");
            MqttPhysicalAdapter mqttPhysicalAdapterVSM = builderVSM.build("vsm-mqtt-pa");
            HttpDigitalAdapterConfiguration configVsm = new HttpDigitalAdapterConfiguration("my-http-adapter", "localhost", 8081);
            HttpDigitalAdapter httpDigitalAdapterVsm = new HttpDigitalAdapter(configVsm, digitalTwinVSM);
            digitalTwinVSM.addPhysicalAdapter(mqttPhysicalAdapterVSM);
            digitalTwinVSM.addDigitalAdapter(new VSMDigitalAdapter("vsm-digital-adapter"));
            digitalTwinVSM.addDigitalAdapter(httpDigitalAdapterVsm);
            // Create the Digital Twin Engine
            DigitalTwinEngine digitalTwinEngine = new DigitalTwinEngine();

            // Add the Digital Twin to the Engine
            //digitalTwinEngine.addDigitalTwin(digitalTwin);

            // Directly start when you add it passing a second boolean value = true
            digitalTwinEngine.addDigitalTwin(digitalTwin, true);
            digitalTwinEngine.addDigitalTwin(digitalTwinVSM, true);

            // Starting the single DT on the engine through its id
            // digitalTwinEngine.startDigitalTwin(id);

            // Start all the DTs registered on the engine
            //digitalTwinEngine.startAll();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}