package org.example.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

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

}
