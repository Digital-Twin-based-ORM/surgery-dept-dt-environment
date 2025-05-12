package org.example.domain;

import org.example.domain.model.HospitalizationRegime;
import org.example.domain.model.PatientModel;

import java.util.Date;
import java.util.List;

public interface SurgeriesAggregateInterface {

    void addPatient(PatientModel patient);
    void setProgrammedDate(String id, Date programmedDate);
    void setExecutionDate(String id, Date executionDate);
    int getWaitingListSize();
    int getWaitingListFiltered(HospitalizationRegime hospitalizationRegime);
    List<PatientModel> getPatientsBeyondTreshold();

}
