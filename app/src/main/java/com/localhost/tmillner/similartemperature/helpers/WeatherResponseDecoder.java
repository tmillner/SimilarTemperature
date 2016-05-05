package com.localhost.tmillner.similartemperature.helpers;

import android.util.Log;

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
            Log.w(TAG, "Couldn't get weather " + e.getLocalizedMessage());
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
            Log.w(TAG, "Couldn't get temperature " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return temp;
    }

    public static String getCountry(JSONObject jsonObject) {
        String country = "";
        try {
            country = (String)
                    ((JSONObject)
                            ((JSONArray) jsonObject.get("sys")).get(0))
                            .get("country");
        } catch (JSONException e) {
            Log.w(TAG, "Couldn't get country " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return country;
    }
}
