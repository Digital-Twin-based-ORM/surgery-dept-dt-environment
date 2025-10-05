package org.example.infrastructureLayer.properties;

import org.example.domain.model.DailySlot;
import org.example.domain.model.SingleSlot;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// Le classi di mappatura sono le stesse dell'esempio di Jackson,
// ma non hanno bisogno delle annotazioni @JsonProperty
public class DailySlotsYamlReader {

    // Modella la struttura del file YAML
    public static class Schedule {
        private List<OperatingRoom> sale_operatorie;

        public List<OperatingRoom> getSale_operatorie() {
            return sale_operatorie;
        }

        public void setSale_operatorie(List<OperatingRoom> sale_operatorie) {
            this.sale_operatorie = sale_operatorie;
        }
    }

    // Modella una singola sala operatoria
    public static class OperatingRoom {
        private String nome;
        private List<DailySlot> slot_giornalieri;

        public String getNome() {
            return nome;
        }

        public void setNome(String nome) {
            this.nome = nome;
        }

        public List<DailySlot> getSlot_giornalieri() {
            return slot_giornalieri;
        }

        public void setSlot_giornalieri(List<DailySlot> slot_giornalieri) {
            this.slot_giornalieri = slot_giornalieri;
        }
    }

    public static Map<String, DailySlot> readDailySlots(String roomName) {
        String date = LocalDate.now().toString();
        HashMap<String, DailySlot> slots = new HashMap<>();
        Yaml yaml = new Yaml();
        try {
            // Crea un file temporaneo con il contenuto YAML
            File yamlFile = new File("src/main/resources/daily_slots.yml");

            // Legge il file YAML e lo deserializza nell'oggetto Schedule
            FileReader reader = new FileReader(yamlFile);
            Schedule schedule = yaml.loadAs(reader, Schedule.class);
            reader.close();

            // Stampa i dati per verificare la lettura
            System.out.println("Dati letti dal file YAML con SnakeYAML:");
            for (OperatingRoom room : schedule.sale_operatorie) {
                System.out.println("--- Sala: " + room.nome + " ---");
                for (DailySlot dailySlot : room.slot_giornalieri) {
                    System.out.println("  Giorno: " + dailySlot.day);
                    for (SingleSlot timeSlot : dailySlot.slots) {
                        System.out.println("    Slot: " + timeSlot.getStartSlot() + " - " + timeSlot.getEndSlot());
                    }
                    if(Objects.equals(roomName, room.nome)) {
                        slots.put(dailySlot.getDay(), dailySlot);
                    }
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return slots;
    }

    public static void main(String[] args) {
        readDailySlots("or_1");
    }
}

