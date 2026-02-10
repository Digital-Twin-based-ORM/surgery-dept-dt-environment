package org.example.utils;

import com.google.gson.*;
import org.example.businessLayer.adapter.OperatingRoomDailySlot;
import org.example.domain.model.DailySlot;
import org.example.domain.model.SingleSlot;
import org.example.domain.model.SurgeryEventInTime;
import org.example.domain.model.SurgeryEvents;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class UtilsFunctions {

    public static DateTimeFormatter STANDARD_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static JsonObject stringToJsonObjectGson(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, JsonObject.class);
        } catch (JsonParseException e) {
            System.out.println("Errore durante la conversione della stringa in JsonObject: " + e.getMessage());
            return null;
        }
    }

    public static String getJsonField(String json, String field) {
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

    public static long getJsonLongField(String json, String field) {
        Gson gson = new Gson();
        try {
            JsonObject jsonObj = stringToJsonObjectGson(json);
            assert jsonObj != null;
            return jsonObj.get(field).getAsLong();
        } catch (JsonParseException e) {
            System.out.println("Errore: ${e.message}");
            return 0;
        }
    }

    public static DailySlot getDailySlotsFromJson(String content) {
        JsonObject json = stringToJsonObjectGson(content);
        assert json != null;
        return findDailySlots(json);
    }

    public static DailySlot getDailySlotsFromJson(JsonObject json) {
        assert json != null;
        return findDailySlots(json);
    }

    private static DailySlot findDailySlots(JsonObject json) {
        try {
            System.out.println(json);
            ArrayList<SingleSlot> dailySlots = new ArrayList<>();
            String day = json.get("day").getAsString();
            JsonArray slots = json.getAsJsonArray("slots");
            for (JsonElement slot : slots.asList()) {
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
        } catch (Exception e) {
            System.out.println("ERROR parsing daily slots: " + e.getMessage());
            throw e;
        }
    }

    public static String convertSlotToJson(OperatingRoomDailySlot roomDailySlot) {
        JsonObject obj = new JsonObject();
        obj.addProperty("operatingRoomId", roomDailySlot.operatingRoomId());
        obj.addProperty("day", roomDailySlot.dailySlot().getLocalDateDay().toString());
        JsonArray slots = new JsonArray();
        for(SingleSlot singleSlot : roomDailySlot.dailySlot().getSlots()) {
            JsonObject slot = new JsonObject();
            slot.addProperty("startSlot", singleSlot.getStartSlot());
            slot.addProperty("endSlot", singleSlot.getEndSlot());
            slot.addProperty("procedure", singleSlot.getProcedure());
            slots.add(slot);
        }
        obj.add("slots", slots);
        return obj.toString();
    }

    public static SurgeryEventInTime surgeryEventInTimeFromJson(String content) {
        SurgeryEvents event = SurgeryEvents.valueOf(getJsonField(content, "event"));
        String id = getJsonField(content, "idSurgery");
        String timestamp = getJsonField(content, "timestamp");
        return new SurgeryEventInTime(id, event, timestamp);
    }

    public static Long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }
}
