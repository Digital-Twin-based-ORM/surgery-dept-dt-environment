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

}
