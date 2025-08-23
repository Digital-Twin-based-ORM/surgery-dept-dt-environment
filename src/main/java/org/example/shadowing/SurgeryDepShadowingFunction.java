package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import org.example.businessLayer.adapter.OperatingRoomDailySlot;
import org.example.businessLayer.adapter.SurgeryKpiNotification;
import org.example.domain.model.*;
import org.example.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;

public class SurgeryDepShadowingFunction extends AbstractShadowing {
    private final ArrayList<Surgery> surgeries = new ArrayList<>(); // TODO LDA (Lista d'Attesa) dalla quale generare la nota operatoria giornaliera su DB
    private Map<String, DailySlot> dailySlots = new HashMap<>();
    private ArrayList<SurgeryLocation> surgeriesExecutedDaily = new ArrayList<>(); // with surgery id and operation room id
    private static final Logger logger = LoggerFactory.getLogger(SurgeryDepShadowingFunction.class);
    private Map<String, Float> floatKpi = new HashMap<>();

    // TODO relationship delle sale operatorie per sapere quali sono presenti nel dipartimento operatorio (con le istanze)

    public SurgeryDepShadowingFunction(String id) {
        super(id);
    }

    @Override
    public Logger getLogger() {
        return null;
    }

    @Override
    protected void onCreate() {
        this.initializeKpiList();
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
                    SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                    this.updateKpiOnSurgery(body.surgeryId(), M10, body.value());
                }
                case M14: {
                    SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                    this.updateKpiOnSurgery(body.surgeryId(), M14, body.value());
                }
                case M15: {
                    SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                    this.updateKpiOnSurgery(body.surgeryId(), M15, body.value());
                }
                case M17: {
                    SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                    this.updateKpiOnSurgery(body.surgeryId(), M17, body.value());
                }
                case M26: {
                    SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                    this.updateKpiOnSurgery(body.surgeryId(), M26, body.value());
                }
                case SLOT_SET: {
                    // set the daily slots of the current working day and the specific operating room
                    OperatingRoomDailySlot slotTemp = (OperatingRoomDailySlot)physicalAssetEventWldtEvent.getBody();
                    if(LocalDate.now().equals(slotTemp.dailySlot().getDay())) {
                        this.dailySlots.put(slotTemp.operatingRoomId(), slotTemp.dailySlot());
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
                    // delete all surgery location (executed daily) and prepare all for the next operative day
                    this.initializeKpiList();
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

    private void initializeKpiList() {
        this.floatKpi.keySet().addAll(List.of(M1, M2, M3, M9, M10, M11, M12, M13, M14, M15, M16, M17, M18, M19, M20, M21, M22, M23, M24, M26, M29));
    }

    private void updateKpiOnSurgery(String surgeryId, String kpi, Float value) {
        Pair<Integer, Surgery> surgery = this.getSurgeryById(surgeryId);
        surgery.getRight().setKpi(kpi, value);
    }

    private Pair<Integer, Surgery> getSurgeryById(String id) {
        Optional<Pair<Integer, Surgery>> surgeryOpt = this.surgeries.stream().filter(i -> Objects.equals(i.getIdSurgery(), id)).map(i -> new Pair<Integer, Surgery>(surgeries.indexOf(i), i)).findFirst();
        if(surgeryOpt.isPresent()) {
            return surgeryOpt.get();
        } else {
            // TODO throw a custom exception
            throw new IllegalArgumentException();
        }
    }
}
