package com.localhost.tmillner.similartemperature;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.localhost.tmillner.similartemperature.db.WeatherContract;
import com.localhost.tmillner.similartemperature.db.WeatherHelper;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultsActivity extends AppCompatActivity {

    private final static String TAG = ResultsActivity.class.getSimpleName();
    private Integer degrees;
    private JSONObject places = new JSONObject();
    private JSONObject matches = new JSONObject();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        this.setListView();
        this.setDegrees();
        this.getLocations();
        try {
            this.findLocationWeatherMatches();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void setListView() {
        listView = (ListView) findViewById(R.id.results);
    }

    public void setDegrees() {
        Intent intent = getIntent();
        String degrees = intent.getStringExtra(WeatherRequest.WEATHER_CURRENT);
        this.degrees = Integer.getInteger(degrees);

        TextView degreesTextView = (TextView) findViewById(R.id.degrees);
        degreesTextView.setText(String.format("°%s  ", degrees.toString()));
    }

    public void getLocations() {
        // First query DB for locations above threshold
        SQLiteDatabase db = new WeatherHelper(this).getReadableDatabase();
        String[] projection = {
                WeatherContract.COLUMN_CITY,
                WeatherContract.COLUMN_COUNTRY,
                WeatherContract.COLUMN_POPULATION
        };

        String whereSelection = WeatherContract.COLUMN_POPULATION + " > ?";
        // WeatherContract.COLUMN_POPULATION is stored as a long, not sure if string will work
        String[] whereArgs= {getString(R.string.preferences_weather_city_min_population_value)};

        String sortOrder = "CASE WHEN " + WeatherContract.COLUMN_POPULATION + " IS NULL " +
                " THEN 0 ELSE 1 END, " + WeatherContract.COLUMN_POPULATION;
        Cursor cursor = db.query(WeatherContract.TABLE,
                projection,
                whereSelection,
                whereArgs,
                null,
                null,
                sortOrder,
                getString(R.string.preferences_weather_api_threshold_per_minute_value));

        // Then query API for locations that match
        cursor.move(-1);
        storeMatches(cursor);
    }

    /**
     * Attempt storage of db matches in results as:
     * {
     *   "results": [{
     *     "city" : "aCity",
     *     "country" : "aCountry",
     *     "population" : "aPopulation"
     *   }]
     * }
     * @param cursor
     */
    private void storeMatches(Cursor cursor) {
        JSONObject[] jsonObjects = {};
        try {
            places.put("results", jsonObjects);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        while(cursor.moveToNext()) {
            JSONObject result = new JSONObject();
            try {
                result.put("city", cursor.getString(cursor.getColumnIndex(WeatherContract.COLUMN_CITY)));
                result.put("country", cursor.getString(cursor.getColumnIndex(WeatherContract.COLUMN_COUNTRY)));
                result.put("population", cursor.getString(cursor.getColumnIndex(WeatherContract.COLUMN_POPULATION)));
                places.accumulate("results",result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void findLocationWeatherMatches() throws JSONException {
        JSONObject[] jsonObjects = {};
        matches.put("results", jsonObjects);
        try {
            for (final JSONObject result : (JSONObject[]) places.get("results")) {
                WeatherRequest.getLocationDataRequest(this,
                        (String) result.get("city"),
                        (String) result.get("country"),
                                new Response.Listener() {
                                    @Override
                                    public void onResponse(Object response) {
                                        // Add items to the local matchesJSON
                                        /* Parse response and retrieve the number */
                                        Log.i(TAG, "Response is: " + response);
                                        Integer responseDegrees = 38;
                                        if (responseDegrees == degrees) {
                                            JSONObject matchingObject = new JSONObject();
                                            try {
                                                matchingObject.put("city", result.get("city"));
                                                matchingObject.put("country", result.get("country"));
                                                matches.accumulate("results", matchingObject);

                                                listView.setAdapter(new WeatherResultListAdapter(
                                                        getApplicationContext(),
                                                        R.layout.content_weather_result_list_adapter,
                                                        (JSONObject[]) matches.get("results")
                                                ));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.w(TAG, "Something Bad happened! " + error);
                                    }
                                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
