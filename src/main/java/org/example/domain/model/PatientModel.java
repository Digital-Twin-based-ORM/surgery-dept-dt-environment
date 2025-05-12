package org.example.domain.model;

import java.util.Date;

public class PatientModel {

    private String id;
    private String priorityClass;
    private HospitalizationRegime hospitalization;
    private String operativeUnit;
    private Date programmedDate;
    private Date executionDate;

    public PatientModel(String priorityClass, HospitalizationRegime hospitalization, String operativeUnit, Date programmedDate, Date executionDate) {
        this.priorityClass = priorityClass;
        this.hospitalization = hospitalization;
        this.operativeUnit = operativeUnit;
        this.programmedDate = programmedDate;
        this.executionDate = executionDate;
    }

    public void setPriorityClass(String priorityClass) {
        this.priorityClass = priorityClass;
    }

    public void setHospitalization(HospitalizationRegime hospitalization) {
        this.hospitalization = hospitalization;
    }

    public void setOperativeUnit(String operativeUnit) {
        this.operativeUnit = operativeUnit;
    }

    public void setProgrammedDate(Date programmedDate) {
        this.programmedDate = programmedDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }

    public String getPriorityClass() {
        return priorityClass;
    }

    public HospitalizationRegime getHospitalization() {
        return hospitalization;
    }

    public String getOperativeUnit() {
        return operativeUnit;
    }

    public Date getProgrammedDate() {
        return programmedDate;
    }

    public Date getExecutionDate() {
        return executionDate;
    }
}
