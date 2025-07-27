package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.example.domain.model.DailySlot;
import org.example.domain.model.Surgery;
import org.example.domain.model.SurgeryEventInTime;
import org.example.domain.model.SurgeryLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;
import static org.example.utils.UtilsFunctions.getJsonField;

public class SurgeryDepShadowingFunction extends AbstractShadowing {
    private final ArrayList<Surgery> surgeries = new ArrayList<>(); // TODO LDA (Lista d'Attesa) dalla quale generare la nota operatoria giornaliera su DB
    private Optional<DailySlot> dailySlot = Optional.empty();
    private ArrayList<SurgeryLocation> surgeriesExecutedDaily = new ArrayList<>(); // with surgery id and operation room id
    private static final Logger logger = LoggerFactory.getLogger(SurgeryDepShadowingFunction.class);

    public SurgeryDepShadowingFunction(String id) {
        super(id);
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String s) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String s, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if (physicalAssetEventWldtEvent != null) {
            logger.info("Event notified... " + physicalAssetEventWldtEvent.getPhysicalEventKey());
            String eventKey = physicalAssetEventWldtEvent.getPhysicalEventKey();
            switch (eventKey) {
                case M10: {

                }
                case M14: {

                }
                case M15: {

                }
                case M17: {

                }
                case M26: {

                }
                case SLOT_SET: {
                    // set the daily slots of the current working day
                    DailySlot slotTemp = (DailySlot)physicalAssetEventWldtEvent.getBody();
                    if(LocalDate.now().equals(slotTemp.getDay())) {
                        this.dailySlot = Optional.of(slotTemp);
                    }
                }
                case NEW_SURGERY_EVENT: {
                    SurgeryEventInTime event = (SurgeryEventInTime)physicalAssetEventWldtEvent.getBody();
                    Optional<Surgery> surgeryOpt = this.surgeries.stream().filter(i -> i.getIdSurgery().equals(event.idSurgery())).findFirst();
                    if(surgeryOpt.isPresent()) {
                        Surgery surgery = surgeryOpt.get();
                        surgery.addTimestamp(event.event(), event.timestamp());
                        surgeries.removeIf(i -> i.getIdSurgery().equals(event.idSurgery()));
                        surgeries.add(surgery);
                    }
                }
                case SURGERY_CREATED: {
                    Surgery surgery = (Surgery)physicalAssetEventWldtEvent.getBody();
                    this.surgeries.add(surgery);
                }
                case SURGERY_PRIORITY_CHANGED: {
                    Surgery surgeryUpdated = (Surgery)physicalAssetEventWldtEvent.getBody();
                    Optional<Surgery> surgeryOpt = this.surgeries.stream().filter(i -> i.getIdSurgery().equals(surgeryUpdated.getIdSurgery())).findFirst();
                    if(surgeryOpt.isPresent()) {
                        Surgery surgery = surgeryOpt.get();
                        surgery.setPriority(surgeryUpdated.getPriority());
                        surgeries.removeIf(i -> i.getIdSurgery().equals(surgery.getIdSurgery()));
                        surgeries.add(surgery);
                    }
                }
                case SURGERY_EXECUTED_IN: {
                    SurgeryLocation surgeryLocation = (SurgeryLocation)physicalAssetEventWldtEvent.getBody();
                    if(this.surgeriesExecutedDaily.stream().noneMatch(i -> i.surgeryId().equals(surgeryLocation.surgeryId()))) {
                        this.surgeriesExecutedDaily.add(surgeryLocation);
                    }
                }
                case WORKING_DAY_TERMINATED: {
                    // TODO calculate KPI on collected data
                    // delete all surgery location and prepare all for the next operative day
                }
            }
        }
    }

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipInstanceDeletedWldtEvent) {

    }

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {

    }
}
