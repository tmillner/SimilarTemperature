package com.localhost.tmillner.similartemperature.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by macbookpro on 4/24/16.
 */
public class WeatherResponseDecoder {

    private void WeatherResponseDecoder() {}

    public static String getWeather(JSONObject jsonObject) {
        String weather = "";
        try {
            weather = (String)
                    ((JSONObject)
                            ((JSONArray) jsonObject.get("weather")).get(0))
                    .get("main");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;
    }
    public static Double getTemperature(JSONObject jsonObject) {
        Double weather = null;
        try {
            weather = (Double) ((JSONObject) jsonObject.get("main")).get("temp");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return weather;
    }
}
