package org.example;

import org.example.domain.model.PriorityClass;
import org.example.domain.model.Surgery;
import org.example.utils.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

import static org.example.physicalAdapter.MqttSurgeryDepPhysicalAdapter.M10;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SurgeriesListTest {

    private final ArrayList<Surgery> surgeries = new ArrayList<>();

    /**
     * Questo metodo viene eseguito prima di ogni singolo metodo di test.
     * Viene usato per inizializzare l'oggetto da testare, assicurando che ogni
     * test parta da una condizione pulita.
     */
    @BeforeEach
    void setUp() {
        this.surgeries.add(new Surgery("1", PriorityClass.A));
    }

    /**
     * Test per il metodo add().
     * Verifica che la somma di due numeri positivi sia corretta.
     */
    @Test
    void testAddPositiveNumbers() {
        // Assertions.assertEquals(valoreAtteso, valoreAttuale, messaggioInCasoDiErrore)
        System.out.println("TESTING....");
        Pair<Integer, Surgery> surgery = this.getSurgeryById("1");
        surgery.getRight().setKpi(M10, 10);
        assertEquals(10, surgeries.getFirst().getKpi(M10));
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
