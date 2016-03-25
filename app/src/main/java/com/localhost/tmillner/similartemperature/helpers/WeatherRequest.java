package com.localhost.tmillner.similartemperature.helpers;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.localhost.tmillner.similartemperature.MainActivity;

import org.json.JSONObject;

/**
 * Created by macbookpro on 3/24/16.
 */
public class WeatherRequest {

    public final static String ERROR_MESSAGE = "com.localhost.tmillner.similartemperature.weather.ERROR";
    public final static String WEATHER_CURRENT = "com.localhost.tmillner.similartemperature.weather.WEATHER_CURRENT";
    private final static String API_DOMAIN = "http://api.openweathermap.org/";


    private static RequestQueue queue;

    public static void sendLocationDataRequest(final Context context){
        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                API_DOMAIN + "data/2.5/weather?q=London,&APPID=dd2ebe434b8f0efa0ec7bc07bdbe7874&units=imperial",
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        /* TODO parse this out to get location data */
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra(WEATHER_CURRENT, "16");
                        context.startActivity(intent);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        /* Send back to the main search screen with an error displayed */
                        Intent intent = new Intent(context, MainActivity.class);
                        intent.putExtra(ERROR_MESSAGE, error.getMessage());
                        context.startActivity(intent);
                    }
                });
    }
}
