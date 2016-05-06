package com.localhost.tmillner.similartemperature.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

/**
 * Created by macbookpro on 4/24/16.
 */
public class WeatherResponseDecoder {

    private static final String TAG = "WeatherResponseDecoder";
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
        Double temp = null;
        try {
            temp = Double.parseDouble(((JSONObject) jsonObject.get("main")).get("temp").toString());
            DecimalFormat df = new DecimalFormat("#.#");
            String fWeather = df.format(temp);
            temp = Double.parseDouble(fWeather);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String getCountry(JSONObject jsonObject) {
        String country = "";
        try {
            country = ((JSONObject) jsonObject.get("sys")).get("country").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return country;
    }
}
