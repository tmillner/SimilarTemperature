package com.localhost.tmillner.similartemperature;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.localhost.tmillner.similartemperature.db.WeatherContract;
import com.localhost.tmillner.similartemperature.db.WeatherHelper;
import com.localhost.tmillner.similartemperature.helpers.ConversionHelper;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;
import com.localhost.tmillner.similartemperature.helpers.WeatherResponseDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ResultsActivity extends AppCompatActivity {

    private final static String TAG = ResultsActivity.class.getSimpleName();
    private Double degrees;
    private JSONObject places = new JSONObject();
    private JSONObject matches = new JSONObject();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        this.setDegrees();
        this.setListView();
        this.getLocations();
        try {
            this.findLocationWeatherMatches();
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this,SettingsActivity.class ));
        }

        return super.onOptionsItemSelected(item);
    }

    private void setListView() {
        listView = (ListView) findViewById(R.id.results);
    }

    public void setDegrees() {
        Intent intent = getIntent();
        String degrees = intent.getStringExtra(WeatherRequest.WEATHER_CURRENT);
        this.degrees = Double.parseDouble(degrees);

        TextView degreesTextView = (TextView) findViewById(R.id.degrees);
        degreesTextView.setText(String.format("°%s  ", degrees));
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
        try {
            places.put("results", new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        while(cursor.moveToNext()) {
            Log.d(TAG, "~~~storeMatches - Hit a match");
            JSONObject result = new JSONObject();
            try {
                result.put("city", cursor.getString(cursor.
                        getColumnIndex(WeatherContract.COLUMN_CITY)));
                result.put("country", cursor.getString(cursor.
                        getColumnIndex(WeatherContract.COLUMN_COUNTRY)));
                result.put("population", cursor.getString(cursor.
                        getColumnIndex(WeatherContract.COLUMN_POPULATION)));
                places.accumulate("results",result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void findLocationWeatherMatches() throws JSONException {
        matches.put("results", new JSONArray());
        final Double degrees = this.degrees;
        try {
            for (int i = 0; i < ((JSONArray) places.get("results")).length(); i++) {
                final JSONObject result = (JSONObject) ((JSONArray) places.get("results")).get(i);
                Log.d(TAG, "~~~A Result is " + result.toString());
                WeatherRequest.getLocationDataRequest(this,
                        (String) result.get("city"),
                        (String) result.get("country"),
                                new Response.Listener() {
                                    @Override
                                    public void onResponse(Object response) {
                                        // Add items to the local matchesJSON
                                        /* Parse response and retrieve the number */
                                        Log.d(TAG, "~~~A resonse is " + response);
                                        Log.d(TAG, "Weather is " + WeatherResponseDecoder.getWeather((JSONObject) response));
                                        Log.d(TAG, "Temp is " + WeatherResponseDecoder.getTemperature((JSONObject) response));
                                        Double responseDegrees = 38d;
                                        if (responseDegrees.equals(degrees)) {
                                            JSONObject matchingObject = new JSONObject();
                                            try {
                                                matchingObject.put("city", result.get("city"));
                                                matchingObject.put("country", result.get("country"));
                                                matches.accumulate("results", matchingObject);

                                                listView.setAdapter(new WeatherResultListAdapter(
                                                        getApplicationContext(),
                                                        R.layout.content_weather_result_list_adapter,
                                                        convertJSONArrayToJSONObjectArray(
                                                                (JSONArray) matches.get("results"))
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

    private JSONObject[] convertJSONArrayToJSONObjectArray(JSONArray jsonArray) {
        JSONObject[] jsonObjectArray = new JSONObject[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObjectArray[i] = (JSONObject) jsonArray.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObjectArray;
    }

    public void convertToFahrenheit(View source) {
        TextView degreesView = (TextView) findViewById(R.id.degrees);
        degreesView.setText("" + ConversionHelper.celsiusToFahrenheit(degrees));
        Button fahrenheitButton = (Button) findViewById(R.id.fahrenheit_button);
        fahrenheitButton.setEnabled(false);
        Button celsiusButton = (Button) findViewById(R.id.celsius_button);
        celsiusButton.setEnabled(true);
    }

    public void convertToCelsius(View source) {
        TextView degreesView = (TextView) findViewById(R.id.degrees);
        degreesView.setText("" + ConversionHelper.fahrenheitToCelsius(degrees));
        Button celsiusButton = (Button) findViewById(R.id.celsius_button);
        celsiusButton.setEnabled(false);
        Button fahrenheitButton = (Button) findViewById(R.id.fahrenheit_button);
        fahrenheitButton.setEnabled(true);
    }
}
