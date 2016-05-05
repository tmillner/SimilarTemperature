package com.localhost.tmillner.similartemperature.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;
import com.localhost.tmillner.similartemperature.MainActivity;
import com.localhost.tmillner.similartemperature.R;
import com.localhost.tmillner.similartemperature.ResultsActivity;
import com.localhost.tmillner.similartemperature.SettingsActivity;

import org.json.JSONObject;

/**
 * Created by macbookpro on 3/24/16.
 */
public class WeatherRequest {

    public final static String TAG = WeatherRequest.class.getSimpleName();
    public final static String ERROR_MESSAGE = "com.localhost.tmillner.similartemperature.weather.ERROR";
    public final static String WEATHER_CURRENT = "com.localhost.tmillner.similartemperature.weather.WEATHER_CURRENT";
    public final static String TEMPERATURE_CURRENT = "com.localhost.tmillner.similartemperature.weather.TEMPERATURE_CURRENT";
    public final static String QUERY_COUNTRY = "com.localhost.tmillner.similartemperature.weather.QUERY_COUNTRY";
    private final static String API_DOMAIN = "http://api.openweathermap.org/";
    private String api_key = "";
    private String temp_units;

    private static RequestQueue queue;

    private WeatherRequest(String api_key, String temp_units) {
        this.api_key = api_key;
        this.temp_units = temp_units;
    }

    public static WeatherRequest getWeatherRequest(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String api_key = sharedPreferences.getString(
                SettingsActivity.OPEN_WEATHER_API_KEY, context.getString(
                        R.string.default_settings_open_weather_api_key));
        String temp_units = sharedPreferences.getString(
                SettingsActivity.TEMPERATURE_METRIC, context.getString(
                        R.string.default_settings_temperature_metric_value));
        return new WeatherRequest(api_key, temp_units);
    }

    public final Double round(Double temperature) {
        // see: R.string.settings_temperature_metric_array_values
        if (this.temp_units.equals("imperial")) {
            return Double.parseDouble("" + Math.round(temperature));
        }
        return temperature;
    }

    public JsonRequest locationDataRequest(final Context context, String city, String country,
                                           Response.Listener listener,
                                           Response.ErrorListener errorListener){
        Log.i(TAG, "Units are " + temp_units);
        String cityUrl = "data/2.5/weather?q=%s,%s&APPID=" + api_key + "&units=" + temp_units;
        return request(context, cityUrl, city, country, listener, errorListener);
    }

    public JsonRequest zipDataRequest(final Context context, String zip, String country,
                                      Response.Listener listener,
                                      Response.ErrorListener errorListener){
        Log.i(TAG, "Units are " + temp_units);
        String zipCodeUrl = "data/2.5/weather?zip=%s,%s&APPID=" + api_key + "&units=" + temp_units;
        return request(context, zipCodeUrl, zip, country, listener, errorListener);
    }

    private JsonRequest request(final Context context, String url, String location,
                                       String country,
                                       Response.Listener listener,
                                       Response.ErrorListener errorListener){
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_DOMAIN + String.format(
                        url,
                        location.replace(" ", "%20"),
                        country.replace(" ", "%20")
                ),
                null,
                listener,
                errorListener);
        queue.add(jsonRequest);
        return jsonRequest;
    }

    public void sendLocationDataRequest(final Context context, String city, String country){
        locationDataRequest(context, city, country, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Double temperature = WeatherResponseDecoder.getTemperature(response);
                String weather = WeatherResponseDecoder.getWeather(response);
                String country = WeatherResponseDecoder.getCountry(response);
                temperature = round(temperature);
                Log.i(TAG, "Response for sendLocationDataRequest is " + temperature);
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra(TEMPERATURE_CURRENT, temperature.toString());
                intent.putExtra(WEATHER_CURRENT, weather);
                intent.putExtra(QUERY_COUNTRY, country);
                context.startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* Send back to the main search screen with an error displayed */
                Log.w(TAG, "Hit an error on weather request");
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(ERROR_MESSAGE, error.getMessage());
                context.startActivity(intent);
            }
        });
    }

    public void sendZipDataRequest(final Context context, String zip, String country){
        zipDataRequest(context, zip, country, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Double temperature = WeatherResponseDecoder.getTemperature(response);
                String weather = WeatherResponseDecoder.getWeather(response);
                String country = WeatherResponseDecoder.getCountry(response);
                temperature = round(temperature);
                Log.i(TAG, "Response for sendLocationDataRequest is " + temperature);
                Intent intent = new Intent(context, ResultsActivity.class);
                intent.putExtra(TEMPERATURE_CURRENT, temperature.toString());
                intent.putExtra(WEATHER_CURRENT, weather);
                intent.putExtra(QUERY_COUNTRY, country);
                context.startActivity(intent);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* Send back to the main search screen with an error displayed */
                Log.w(TAG, "Hit an error on weather request");
                Intent intent = new Intent(context, MainActivity.class);
                intent.putExtra(ERROR_MESSAGE, error.getMessage());
                context.startActivity(intent);
            }
        });
    }

    public void getLocationDataRequest(final Context context, String city, String country,
                                              Response.Listener responseListener,
                                              Response.ErrorListener errorListener){
        locationDataRequest(context, city, country, responseListener, errorListener);
    }
}
