package org.example.digitalAdapter.configuration;

public class MQTTAdapterConfiguration {

    private String baseTopic;

    private String broker;

    private String clientId;

    public MQTTAdapterConfiguration(String baseTopic, String broker, String clientId) {
        this.baseTopic = baseTopic;
        this.broker = broker;
        this.clientId = clientId;
    }

    public String getBaseTopic() {
        return baseTopic;
    }

    public String getBroker() {
        return broker;
    }

    public String getClientId() {
        return clientId;
    }

    public void setBaseTopic(String baseTopic) {
        this.baseTopic = baseTopic;
    }

    public void setBroker(String broker) {
        this.broker = broker;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return "VSMAdapterConfiguration{" +
                ", baseTopic='" + baseTopic + '\'' +
                ", broker='" + broker + '\'' +
                ", clientId='" + clientId + '\'' +
                '}';
    }
}
