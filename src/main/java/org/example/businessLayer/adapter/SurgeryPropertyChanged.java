package org.example.businessLayer.adapter;

public class SurgeryPropertyChanged<T> {
    private String idSurgery;
    private T value;

    public SurgeryPropertyChanged(String idSurgery, T value) {
        this.idSurgery = idSurgery;
        this.value = value;
    }

    public String getIdSurgery() {
        return idSurgery;
    }

    public T getValue() {
        return value;
    }
}
