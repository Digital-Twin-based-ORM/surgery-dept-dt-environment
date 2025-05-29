package org.example.domain.model;

public class PatientNominative {

    private final String name;
    private final String surname;

    public PatientNominative(String name, String surname) {
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }
}
