package org.example.physicalAdapter;

import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapter;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfiguration;
import it.wldt.adapter.mqtt.physical.MqttPhysicalAdapterConfigurationBuilder;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.example.domain.model.PriorityClass;
import org.example.domain.model.SurgeryEvents;

public class MqttSurgeryPhysicalAdapter {

    public final static String PRIORITY_KEY = "priority";
    public final static String STATUS_KEY = "status";
    MqttPhysicalAdapterConfigurationBuilder builder;

    public MqttSurgeryPhysicalAdapter(String idDT, String host, Integer port) throws MqttPhysicalAdapterConfigurationException {
        this.builder = MqttPhysicalAdapterConfiguration.builder(host, port);
        /*for(SurgeryEvents event: SurgeryEvents.values()) {
            // create a topic for each surgery event
            System.out.println("AGGIUNGO " + event.getName());
            builder.addPhysicalAssetEventAndTopic(event.getName(), "text/plain", "anylogic/id/surgery/" + idDT + "/" + event.getName(), i -> i);
        }*/
        builder.addPhysicalAssetEventAndTopic("updateSurgeryState", "text/plain", "anylogic/id/surgery/" + idDT + "/updateSurgeryState", SurgeryEvents::valueOf);
        builder.addPhysicalAssetPropertyAndTopic(PRIORITY_KEY, "", "anylogic/id/surgery/" + idDT + "/" + PRIORITY_KEY, i -> {
            if(i.equals(PriorityClass.A.toString()) || i.equals(PriorityClass.B.toString()) || i.equals(PriorityClass.C.toString()) || i.equals(PriorityClass.D.toString()))
                return i;
            else
                throw new IllegalArgumentException();
        });
        builder.addPhysicalAssetPropertyAndTopic(STATUS_KEY, "", "anylogic/id/surgery/" + idDT + "/" + STATUS_KEY, i -> i);
    }

    public MqttPhysicalAdapter build(String id) throws MqttPhysicalAdapterConfigurationException, MqttException {
        return new MqttPhysicalAdapter(id, builder.build());
    }

}
