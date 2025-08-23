package org.example.dt.property;

import it.wldt.adapter.physical.PhysicalAssetProperty;
import org.example.domain.model.PatientNominative;

import java.time.LocalDate;
import java.util.Date;

public class PatientProperties extends InternalProperties {

    private final PatientNominative patientNominative;
    private final String gender;
    private final String birthDate;

    public PatientProperties(String name, String surname, String gender, LocalDate birthDate) {
        this.gender = gender;
        this.birthDate = birthDate.toString();
        this.patientNominative = new PatientNominative(name, surname);

        this.addProperty(new PhysicalAssetProperty<>("name", this.patientNominative));
        this.addProperty(new PhysicalAssetProperty<>("gender", this.gender));
        this.addProperty(new PhysicalAssetProperty<>("birthDate", this.birthDate));
    }

    public PatientNominative getPatientNominative() {
        return patientNominative;
    }

    public String getGender() {
        return gender;
    }

    public LocalDate getBirthDate() {
        return LocalDate.parse(birthDate);
    }
}
