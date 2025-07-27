package org.example.utils;

import com.google.gson.*;
import org.example.domain.model.DailySlot;
import org.example.domain.model.SingleSlot;

import java.util.ArrayList;

public class UtilsFunctions {

    public static JsonObject stringToJsonObjectGson(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, JsonObject.class);
        } catch (JsonParseException e) {
            System.out.println("Errore durante la conversione della stringa in JsonObject: ${e.message}");
            return null;
        }
    }

    public static String getJsonField(String json, String field) {
        Gson gson = new Gson();
        try {
            JsonObject jsonObj = stringToJsonObjectGson(json);
            assert jsonObj != null;
            return jsonObj.get(field).getAsString();
        } catch (JsonParseException e) {
            System.out.println("Errore: ${e.message}");
            return null;
        }
    }

    public static Integer getJsonIntField(String json, String field) {
        Gson gson = new Gson();
        try {
            JsonObject jsonObj = stringToJsonObjectGson(json);
            assert jsonObj != null;
            return jsonObj.get(field).getAsInt();
        } catch (JsonParseException e) {
            System.out.println("Errore: ${e.message}");
            return null;
        }
    }

    public static DailySlot getDailySlotsFromJson(String content) {
        ArrayList<SingleSlot> dailySlots = new ArrayList<>();
        JsonObject json = stringToJsonObjectGson(content);
        assert json != null;
        String day = json.get("day").getAsString();
        JsonArray slots = json.getAsJsonArray("slots");
        for(JsonElement slot : slots.asList()) {
            JsonObject slotObj = slot.getAsJsonObject();
            dailySlots.add(
                    new SingleSlot(
                            slotObj.get("startSlot").getAsString(),
                            slotObj.get("endSlot").getAsString(),
                            slotObj.get("procedure").getAsString()
                    )
            );
        }
        return new DailySlot(day, dailySlots);
    }

}
