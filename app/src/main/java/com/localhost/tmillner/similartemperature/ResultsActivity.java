package com.localhost.tmillner.similartemperature;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.localhost.tmillner.similartemperature.db.CountriesContract;
import com.localhost.tmillner.similartemperature.db.WeatherContract;
import com.localhost.tmillner.similartemperature.db.WeatherHelper;
import com.localhost.tmillner.similartemperature.helpers.ConversionHelper;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;
import com.localhost.tmillner.similartemperature.helpers.WeatherResponseDecoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;

public class ResultsActivity extends AppCompatActivity {

    private final static String TAG = ResultsActivity.class.getSimpleName();
    private Double degrees;
    private String weather;
    private String queryCountry;
    private String queryRegion;
    private JSONObject places = new JSONObject();
    private JSONObject matches = new JSONObject();
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        this.setCurrentWeather();
        this.setTemperatureButtons();
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
        this.setTemperatureButtons();
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

    private void storeUserQuery(String query, String country, String weather, String degrees) {
        try {
            FileOutputStream outputStream = openFileOutput(MainActivity.STORAGE_FILE, Context.MODE_APPEND);
            outputStream.write(String.format("%s,%s,%s,%s\n", query, country, weather, degrees).getBytes());
            outputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setCurrentWeather() {
        Intent intent = getIntent();
        String degrees = intent.getStringExtra(WeatherRequest.TEMPERATURE_CURRENT);
        this.degrees = Double.parseDouble(degrees);
        this.weather = intent.getStringExtra(WeatherRequest.WEATHER_CURRENT);
        this.queryCountry = intent.getStringExtra(WeatherRequest.QUERY_COUNTRY);
        this.queryRegion = intent.getStringExtra(WeatherRequest.QUERY_REGION);
        storeUserQuery(this.queryRegion, this.queryCountry, this.weather, degrees);

        TextView degreesTextView = (TextView) findViewById(R.id.degrees);
        degreesTextView.setText(String.format("°%s  ", degrees));
    }

    public void getLocations() {
        // First query DB for locations above threshold
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String minCityPopulationCriteria = sharedPreferences.getString(
                SettingsActivity.MIN_POPULATION_CRITERIA, getString(
                        R.string.default_settings_min_population_criteria_value));
        String limit = getString(R.string.preferences_weather_api_lax_threshold_value);

        SQLiteDatabase db = new WeatherHelper(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " +
                        WeatherContract.COLUMN_CITY + ", " +
                        CountriesContract.COLUMN_COUNTRY_NAME + ", " +
                        WeatherContract.COLUMN_POPULATION + " FROM " + WeatherContract.TABLE +
                        " JOIN " + CountriesContract.TABLE + " ON " +
                        WeatherContract.TABLE + "." + WeatherContract.COLUMN_COUNTRY_ID + "=" +
                        CountriesContract.TABLE + "." + CountriesContract.COLUMN_ID +
                        " WHERE " + WeatherContract.COLUMN_POPULATION + " > ?" +
                        " ORDER BY RANDOM() LIMIT " + limit,
                new String[]{minCityPopulationCriteria}
        );

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
            JSONObject result = new JSONObject();
            try {
                result.put("city", cursor.getString(cursor.
                        getColumnIndex(WeatherContract.COLUMN_CITY)));
                result.put("country", cursor.getString(cursor.
                        getColumnIndex(CountriesContract.COLUMN_COUNTRY_NAME)));
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
        final int placesLength = ((JSONArray) places.get("results")).length();
        try {
            for (int i = 0; i < placesLength; i++) {
                final JSONObject result = (JSONObject) ((JSONArray) places.get("results")).get(i);
                final Boolean isLastResult = (i == (placesLength - 1));
                WeatherRequest weatherRequest= WeatherRequest.getWeatherRequest(this);
                weatherRequest.getLocationDataRequest(this,
                        (String) result.get("city"),
                        (String) result.get("country"),
                                new Response.Listener() {
                                    @Override
                                    public void onResponse(Object response) {
                                        // Add items to the local matchesJSON
                                        String weather =  WeatherResponseDecoder.getWeather((JSONObject) response);
                                        Double responseDegrees = round(WeatherResponseDecoder.getTemperature((JSONObject) response));
                                        if (responseDegrees.equals(degrees)) {
                                            JSONObject matchingObject = new JSONObject();
                                            try {
                                                matchingObject.put("city", result.get("city"));
                                                matchingObject.put("country", result.get("country"));
                                                matchingObject.put("weather", weather);
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
                                        if (isLastResult) {
                                            try {
                                                if (((JSONArray) matches.get("results")).length() == 0) {
                                                    replaceListView();
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        error.printStackTrace();
                                    }
                                });
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private final void replaceListView() {
        View results = findViewById(R.id.results_list);
        ViewGroup parent = (ViewGroup) results.getParent();
        int index = parent.indexOfChild(results);
        parent.removeView(results);

        TextView noResultsTextView = new TextView(this);
        String[] noResults = getResources().getStringArray(R.array.results_no_results);
        Double rand = Math.random();
        int i = (int) Math.round((noResults.length -1) * rand);

        noResultsTextView.setGravity(Gravity.CENTER);
        noResultsTextView.setText(noResults[i]);
        parent.addView(noResultsTextView, index);
    }

    private Double round(Double temperature) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String temp_units = sharedPreferences.getString(
                SettingsActivity.TEMPERATURE_METRIC, getString(
                        R.string.default_settings_temperature_metric_value));
        // see: R.string.settings_temperature_metric_array_values
        if (temp_units.equals("imperial")) {
            return Double.parseDouble("" + Math.round(temperature));
        }
        return temperature;
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String temp_units = sharedPreferences.getString(
                SettingsActivity.TEMPERATURE_METRIC, getString(
                        R.string.default_settings_temperature_metric_value));
        if (temp_units.equals("imperial")) {
            degreesView.setText(degrees.toString());
        } else {
            degreesView.setText("" + ConversionHelper.celsiusToFahrenheit(degrees));
        }

        enableMetricButton(R.id.celsius_button);
    }

    public void convertToCelsius(View source) {
        TextView degreesView = (TextView) findViewById(R.id.degrees);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String temp_units = sharedPreferences.getString(
                SettingsActivity.TEMPERATURE_METRIC, getString(
                        R.string.default_settings_temperature_metric_value));
        if (temp_units.equals("imperial")) {
            degreesView.setText("" + ConversionHelper.fahrenheitToCelsius(degrees));
        } else {
            degreesView.setText(degrees.toString());
        }

        enableMetricButton(R.id.fahrenheit_button);
    }

    private void setTemperatureButtons() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String temp_units = sharedPreferences.getString(
                SettingsActivity.TEMPERATURE_METRIC, getString(
                        R.string.default_settings_temperature_metric_value));
        if (temp_units.equals("imperial")) {
            enableMetricButton(R.id.celsius_button);
        } else {
            enableMetricButton(R.id.fahrenheit_button);
        }
    }

    private void enableMetricButton(Integer resource) {
        Button celsiusButton = (Button) findViewById(R.id.celsius_button);
        Button fahrenheitButton = (Button) findViewById(R.id.fahrenheit_button);
        if (resource == R.id.fahrenheit_button) {
            fahrenheitButton.setEnabled(true);
            celsiusButton.setEnabled(false);
        } else {
            celsiusButton.setEnabled(true);
            fahrenheitButton.setEnabled(false);
        }
    }
}
