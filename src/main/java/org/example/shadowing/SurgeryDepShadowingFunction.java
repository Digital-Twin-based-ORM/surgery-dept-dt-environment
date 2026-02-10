package org.example.shadowing;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.mqtt.physical.exception.MqttPhysicalAdapterConfigurationException;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.PhysicalAssetProperty;
import it.wldt.adapter.physical.PhysicalAssetRelationship;
import it.wldt.adapter.physical.PhysicalAssetRelationshipInstance;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.DigitalTwinStateRelationship;
import it.wldt.core.state.DigitalTwinStateRelationshipInstance;
import it.wldt.exception.WldtDigitalTwinStateEventNotificationException;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.example.businessLayer.adapter.KpiDigitalRecord;
import org.example.businessLayer.adapter.KpiPropertyRecord;
import org.example.businessLayer.adapter.OperatingRoomDailySlot;
import org.example.businessLayer.adapter.SurgeryKpiNotification;
import org.example.domain.model.*;
import org.example.utils.KpiCalculator;
import org.example.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.example.digitalAdapter.sql.KpiRepositorylDigitalAdapter.OPERATING_ROOM_KPI_UPDATE;
import static org.example.digitalAdapter.sql.KpiRepositorylDigitalAdapter.SURGERY_KPI_UPDATE;
import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.*;
import static org.example.utils.GlobalValues.*;

public class SurgeryDepShadowingFunction extends AbstractShadowing {
    private final Logger logger = LoggerFactory.getLogger(SurgeryDepShadowingFunction.class);
    private final ArrayList<Surgery> surgeries = new ArrayList<>(); // TODO LDA (Lista d'Attesa) dalla quale generare la nota operatoria giornaliera su DB
    private Map<String, DailySlot> dailySlots = new HashMap<>();
    private ArrayList<SurgeryLocation> surgeriesExecutedDaily = new ArrayList<>(); // with surgery id and operation room id
    private Map<String, Float> floatKpi = new HashMap<>();
    private List<String> idOperatingRooms;
    private PhysicalAssetRelationship<String> surgerySupervisedRelationship = null;
    private PhysicalAssetRelationship<String> operatingRoomsRelationship = null;

    public SurgeryDepShadowingFunction(String id, List<String> idOperatingRooms) {
        super(id);
        // TODO popolare la lista idOperatingRooms in base alle relationships
        this.idOperatingRooms = idOperatingRooms;
    }

    @Override
    public Logger getLogger() {
        return logger;
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
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        PhysicalAssetDescription relationshipsPad = new PhysicalAssetDescription();
        this.surgerySupervisedRelationship = new PhysicalAssetRelationship<>(SUPERVISE_SURGERY_NAME, SUPERVISE_SURGERY_TYPE);
        this.operatingRoomsRelationship = new PhysicalAssetRelationship<>(OPERATING_ROOMS_NAME, OPERATING_ROOMS_TYPE);
        relationshipsPad.getRelationships().add(this.surgerySupervisedRelationship);
        relationshipsPad.getRelationships().add(this.operatingRoomsRelationship);

        List<String> kpiNames = List.of(M1, M2, M3, M9, M10, M11, M12, M13, M14, M15, M16, M17, M18, M19, M20, M21, M22, M23, M24, M26, M29);
        kpiNames.forEach(i -> {
            relationshipsPad.getProperties().add(new PhysicalAssetProperty<List<KpiPropertyRecord>>("KPI_" + i, List.of()));
        });
        adaptersPhysicalAssetDescriptionMap.put("relationshipsPad", relationshipsPad);
        super.onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String s) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String s, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {
        super.onPhysicalAssetPropertyVariation(physicalAssetPropertyWldtEvent);
    }

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
        if (physicalAssetEventWldtEvent != null) {
            logger.info("Event notified... " + physicalAssetEventWldtEvent.getPhysicalEventKey());
            String eventKey = physicalAssetEventWldtEvent.getPhysicalEventKey();
            try {
                switch (eventKey) {
                    case M10 -> {
                        SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                        this.updateKpiOnSurgery(body.surgeryId(), M10, body.value());
                        notifySurgeryKpi(body, M10, physicalAssetEventWldtEvent.getCreationTimestamp());
                    }
                    case M14 -> {
                        SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                        this.updateKpiOnSurgery(body.surgeryId(), M14, body.value());
                        notifySurgeryKpi(body, M14, physicalAssetEventWldtEvent.getCreationTimestamp());
                    }
                    case M15 -> {
                        SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                        this.updateKpiOnSurgery(body.surgeryId(), M15, body.value());
                        notifySurgeryKpi(body, M15, physicalAssetEventWldtEvent.getCreationTimestamp());
                    }
                    case M17 -> {
                        SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                        this.updateKpiOnSurgery(body.surgeryId(), M17, body.value());
                        notifySurgeryKpi(body, M17, physicalAssetEventWldtEvent.getCreationTimestamp());
                    }
                    case M26 -> {
                        SurgeryKpiNotification body = (SurgeryKpiNotification) physicalAssetEventWldtEvent.getBody();
                        this.updateKpiOnSurgery(body.surgeryId(), M26, body.value());
                        notifySurgeryKpi(body, M26, physicalAssetEventWldtEvent.getCreationTimestamp());
                    }
                    case SLOT_SET -> {
                        // set the daily slots of the current working day and the specific operating room
                        OperatingRoomDailySlot slotTemp = (OperatingRoomDailySlot) physicalAssetEventWldtEvent.getBody();
                        Optional<DigitalTwinStateProperty<?>> currentDateOpt = this.digitalTwinStateManager.getDigitalTwinState().getProperty(CURRENT_DATE);
                        if(currentDateOpt.isPresent()) {
                            String currentDate = (String)(currentDateOpt.get()).getValue();
                            logger.info("SLOT DATE: " + slotTemp.dailySlot().getDay() + "   CURRENT DATE: " + currentDate);
                            if (slotTemp.dailySlot().isDayEqualsTo(LocalDate.parse(currentDate))) {
                                this.dailySlots.put(slotTemp.operatingRoomId(), slotTemp.dailySlot());
                                logger.info("Id room: " + slotTemp.operatingRoomId());
                                logger.info("Aggiunto slot: " + this.dailySlots.get(slotTemp.operatingRoomId()));

                                // set the operating room relationship if not already defined
                                PhysicalAssetRelationshipInstance<String> relInstance = this.operatingRoomsRelationship.createRelationshipInstance(slotTemp.operatingRoomId());
                                this.digitalTwinStateManager.startStateTransaction();
                                this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(relInstance.getRelationship().getName(), relInstance.getTargetId(), relInstance.getKey()));
                                this.digitalTwinStateManager.commitStateTransaction();
                            }
                        }
                    }
                    case NEW_SURGERY_EVENT -> {
                        SurgeryEventInTime event = (SurgeryEventInTime) physicalAssetEventWldtEvent.getBody();
                        Optional<Surgery> surgeryOpt = this.surgeries.stream().filter(i -> i.getIdSurgery().equals(event.idSurgery())).findFirst();
                        if (surgeryOpt.isPresent()) {
                            Surgery surgery = surgeryOpt.get();
                            surgery.addTimestamp(event.event(), event.timestamp());
                            surgeries.removeIf(i -> i.getIdSurgery().equals(event.idSurgery()));
                            surgeries.add(surgery);
                        }
                    }
                    case SURGERY_CREATED -> {
                        Surgery surgery = (Surgery) physicalAssetEventWldtEvent.getBody();
                        this.surgeries.add(surgery);
                        logger.info("Aggiunto intervento chirurgico");
                        // aggiungere relazione con intervento
                        PhysicalAssetRelationshipInstance<String> relInstance = this.surgerySupervisedRelationship.createRelationshipInstance(surgery.getIdSurgery());
                        this.digitalTwinStateManager.startStateTransaction();
                        this.digitalTwinStateManager.addRelationshipInstance(new DigitalTwinStateRelationshipInstance<>(relInstance.getRelationship().getName(), relInstance.getTargetId(), relInstance.getKey()));
                        this.digitalTwinStateManager.commitStateTransaction();

                    }
                    case SURGERY_PRIORITY_CHANGED -> {
                        Surgery surgeryUpdated = (Surgery) physicalAssetEventWldtEvent.getBody();
                        Optional<Surgery> surgeryOpt = this.surgeries.stream().filter(i -> i.getIdSurgery().equals(surgeryUpdated.getIdSurgery())).findFirst();
                        if (surgeryOpt.isPresent()) {
                            Surgery surgery = surgeryOpt.get();
                            surgery.setPriority(surgeryUpdated.getPriority());
                            surgeries.removeIf(i -> i.getIdSurgery().equals(surgery.getIdSurgery()));
                            surgeries.add(surgery);
                        }
                    }
                    case SURGERY_EXECUTED_IN -> {
                        logger.info("SURGERY EXECUTED EVENT RECEIVED...");
                        SurgeryLocation surgeryLocation = (SurgeryLocation) physicalAssetEventWldtEvent.getBody();
                        if (this.surgeriesExecutedDaily.stream().noneMatch(i -> i.surgeryId().equals(surgeryLocation.surgeryId()))) {
                            this.surgeriesExecutedDaily.add(surgeryLocation);
                        }
                    }
                    case SURGERY_CANCELLED -> {
                        logger.info("SURGERY IS CANCELLED");
                        String surgeryId = (String) physicalAssetEventWldtEvent.getBody();
                        Optional<Surgery> surgeryOpt = this.surgeries.stream().filter(i -> i.getIdSurgery().equals(surgeryId)).findFirst();
                        surgeryOpt.ifPresent(Surgery::cancelSurgery);
                        Optional<Surgery> surgeryOpt1 = this.surgeries.stream().filter(i -> i.getIdSurgery().equals(surgeryId)).findFirst();
                        surgeryOpt1.ifPresent(surgery -> logger.info("SURGERY INFO: " + surgery.isCancelled()));
                    }
                    case WORKING_DAY_STARTED -> {
                        logger.info("** WORKING DAY STARTED **");
                    }
                    case WORKING_DAY_TERMINATED -> {
                        // TODO remove supervised surgery relationships
                        logger.info("** WORKING DAY TERMINATED **");
                        logger.info("Interventi chirurgici: " + surgeries.size());
                        for (Surgery surgery : surgeries) {
                            logger.info("Intervento: " + surgery.getIdSurgery());
                            if(surgery.hasKpiSet(M10)) logger.info(" - M10: " + surgery.getKpi(M10));
                            if(surgery.hasKpiSet(M14)) logger.info(" - M14: " + surgery.getKpi(M14));
                            if(surgery.hasKpiSet(M15)) logger.info(" - M15: " + surgery.getKpi(M15));
                            if(surgery.hasKpiSet(M17)) logger.info(" - M17: " + surgery.getKpi(M17));
                            if(surgery.hasKpiSet(M26)) logger.info(" - M26: " + surgery.getKpi(M26));
                        }

                        KpiCalculator calculator = new KpiCalculator(surgeries, surgeriesExecutedDaily, dailySlots);
                        for(String idOR : idOperatingRooms) {
                            logger.info("Sala operatoria: " + idOR);
                            float m9 = calculator.M9(idOR);
                            notifyOperatingRoomKpi(idOR, m9, M9, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M9: " + m9);
                            float m10 = calculator.M10(idOR);
                            notifyOperatingRoomKpi(idOR, m10, M10, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M10: " + m10);
                            float m11 = calculator.M11(idOR);
                            notifyOperatingRoomKpi(idOR, m11, M11, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M11: " + m11);
                            float m12 = calculator.M12(idOR);
                            notifyOperatingRoomKpi(idOR, m12, M12, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M12: " + m12);
                            float m13 = calculator.M13(idOR);
                            notifyOperatingRoomKpi(idOR, m13, M13, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M13: " + m13);
                            float m16 = calculator.M16(idOR);
                            notifyOperatingRoomKpi(idOR, m16, M16, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M16: " + m16);
                            float m21 = calculator.M21(idOR);
                            notifyOperatingRoomKpi(idOR, m21, M21, physicalAssetEventWldtEvent.getCreationTimestamp());
                            logger.info("M21: " + m21);
                        }
                        float m22 = calculator.M22();
                        notifyOperatingRoomKpi(this.getId(), m22, M22, physicalAssetEventWldtEvent.getCreationTimestamp());
                        logger.info("M22: " + m22);

                        float m24 = calculator.M24();
                        notifyOperatingRoomKpi(this.getId(), m24, M24, physicalAssetEventWldtEvent.getCreationTimestamp());
                        logger.info("M24: " + m24);

                        // delete all surgery location (executed daily) and prepare all for the next operative day
                        this.initializeKpiList();
                        this.removeTerminatedSurgeries();
                        this.resetDailySlots();
                    }
                }
            } catch (Exception e) {
                logger.error("ERROR Department: " + e + " - " + e.getMessage() + " at line ");
                e.printStackTrace();
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
        // TODO only for test -> REMOVE
        logger.info("trying adding KPI property");
        try {
            this.addKpiProperty(new KpiDigitalRecord("id", "KPI_m9", "desc", 1.0f, LocalDateTime.now()));
        } catch (WldtDigitalTwinStatePropertyException | WldtDigitalTwinStateException e) {
            logger.error("Error while adding KPI property", e);
        }
    }

    private void notifyOperatingRoomKpi(String orId, float value, String type, long creationTimestamp) throws WldtDigitalTwinStateEventNotificationException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStateException {
        KpiDigitalRecord kpiDigitalRecord = new KpiDigitalRecord(orId, type, "", value, LocalDateTime.now());
        this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(
                OPERATING_ROOM_KPI_UPDATE,
                kpiDigitalRecord,
                creationTimestamp
        ));
        this.addKpiProperty(kpiDigitalRecord);
    }

    private void notifySurgeryKpi(SurgeryKpiNotification body, String type, long creationTimestamp) throws WldtDigitalTwinStateEventNotificationException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStateException {
        logger.info("NOTIFYING NEW SURGERY KPI...");
        KpiDigitalRecord kpiRecord = new KpiDigitalRecord(body.surgeryId(), type, body.typeOfSurgery(), body.value(), LocalDateTime.now());
        this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(
                SURGERY_KPI_UPDATE,
                kpiRecord,
                creationTimestamp
        ));
        this.addKpiProperty(kpiRecord);
    }

    private void initializeKpiList() {
        //TODO remove?!
        this.floatKpi = new HashMap<>();
        List<String> kpi = List.of(M1, M2, M3, M9, M10, M11, M12, M13, M14, M15, M16, M17, M18, M19, M20, M21, M22, M23, M24, M26, M29);
        kpi.forEach(i -> {
            floatKpi.put(i, 0f);
        });
    }

    /**
     * Remove the executed surgeries from the waiting list.
     */
    private void removeTerminatedSurgeries() {
        List<String> surgeriesExecutedIds = this.surgeriesExecutedDaily.stream().map(SurgeryLocation::surgeryId).toList();
        this.surgeries.removeIf(i -> surgeriesExecutedIds.contains(i.getIdSurgery()));
    }

    /**
     * Clear all the daily slots assigned for the terminated working day.
     */
    private void resetDailySlots() throws WldtDigitalTwinStateException {
        this.dailySlots = new HashMap<>();
        if(this.digitalTwinStateManager.getDigitalTwinState().getRelationshipList().isPresent()) {
            Optional<DigitalTwinStateRelationship<?>> relationship = this.digitalTwinStateManager.getDigitalTwinState().getRelationshipList().get().stream().filter(i -> i.getName().equals(OPERATING_ROOMS_NAME)).findFirst();
            //TODO what the hell is that?!
            if(relationship.isPresent()) {
                List<String> instancesKey = relationship.get().getInstances().stream().map(DigitalTwinStateRelationshipInstance::getKey).toList();
                for(String key : instancesKey) {
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.deleteRelationshipInstance(relationship.get().getName(), key);
                    this.digitalTwinStateManager.commitStateTransaction();
                }
            }
        }
    }

    private void addKpiProperty(KpiDigitalRecord kpiDigitalRecord) throws WldtDigitalTwinStatePropertyException, WldtDigitalTwinStateException {
        String kpiListKey = "KPI_" + kpiDigitalRecord.type();
        Optional<DigitalTwinStateProperty<?>> listOpt = this.digitalTwinStateManager.getDigitalTwinState().getProperty(kpiListKey);
        if(listOpt.isPresent()) {
            DigitalTwinStateProperty<?> property = listOpt.get();
            List<?> list = (List<?>) property.getValue();
            KpiPropertyRecord record = new KpiPropertyRecord(kpiDigitalRecord.id(), kpiDigitalRecord.type(), kpiDigitalRecord.value(), kpiDigitalRecord.timestamp().toString());
            List<KpiPropertyRecord> kpiList = list.stream().map(i -> (KpiPropertyRecord)i).collect(Collectors.toList());
            kpiList.add(record);
            this.updateProperty(kpiListKey, kpiList);
        } else {
            this.updateProperty(kpiListKey, List.of(kpiDigitalRecord));
        }
    }


    private void updateKpiOnSurgery(String surgeryId, String kpi, Float value) {
        // change the surgery and put the new kpi
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
