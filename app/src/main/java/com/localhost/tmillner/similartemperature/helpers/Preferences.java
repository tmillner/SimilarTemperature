package com.localhost.tmillner.similartemperature.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.localhost.tmillner.similartemperature.R;

/**
 * Created by macbookpro on 3/24/16.
 */
public class Preferences {
    private static final String PREFERENCES_NS = "com.localhost.tmillner.similartemperature.preferences.";

    public static void setPreference(Context context, String key, String value){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCES_NS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static void setPreference(Context context, Integer key, String value){
        String stringKey = context.getString(key);
        setPreference(context, stringKey, value);
    }

    public static void setPreference(Context context, Integer key, Integer value){
        String stringKey = context.getString(key);
        String stringValue = context.getString(value);
        setPreference(context, stringKey, stringValue);
    }

    public static String getPreference(Context context, String key){
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                PREFERENCES_NS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, "");
    }

    public static String getPreference(Context context, Integer key){
        String stringKey = context.getString(key);
        return getPreference(context, stringKey);
    }

    public static Boolean isFirstTimeUsingApp(Context context) {
        Boolean isFirstTimeUsingApp = true;
        if (getPreference(context, R.string.preferences_first_time_key) != "") {
            isFirstTimeUsingApp = false;
        }
        return isFirstTimeUsingApp;
    }

    public static void initialize(Context context) {
        setPreference(context, R.string.preferences_first_time_key, "false");
        setPreference(context, R.string.preferences_weather_api_token_key,
                R.string.preferences_weather_api_token_value);
        setPreference(context, R.string.preferences_weather_temperature_unit_key,
                R.string.preferences_weather_temperature_unit_value);
    }
}
