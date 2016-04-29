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
        Double weather = null;
        try {
            weather = (Double) ((JSONObject) jsonObject.get("main")).get("temp");
            DecimalFormat df = new DecimalFormat("#.#");
            String fWeather = df.format(weather);
            weather = Double.parseDouble(fWeather);
        } catch (JSONException e) {
            Log.w(TAG, "Couldn't get temperature " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return weather;
    }
}
