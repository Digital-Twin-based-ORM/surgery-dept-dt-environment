package org.example.domain;

import org.example.domain.model.HospitalizationRegime;
import org.example.domain.model.PatientModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SurgeriesAggregate implements SurgeriesAggregateInterface {

    private ArrayList<PatientModel> waitingList;

    public SurgeriesAggregate() {
        waitingList = new ArrayList<>();
    }

    public void addPatient(PatientModel patient) {
        waitingList.add(patient);
    }

    @Override
    public void setProgrammedDate(String id, Date programmedDate) {

    }

    @Override
    public void setExecutionDate(String id, Date executionDate) {

    }

    @Override
    public int getWaitingListSize() {
        return 0;
    }

    @Override
    public int getWaitingListFiltered(HospitalizationRegime hospitalizationRegime) {
        return 0;
    }

    @Override
    public List<PatientModel> getPatientsBeyondTreshold() {
        return null;
    }


}
