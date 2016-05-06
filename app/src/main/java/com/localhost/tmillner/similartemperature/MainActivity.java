package com.localhost.tmillner.similartemperature;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.places.Places;
import com.localhost.tmillner.similartemperature.helpers.PlacesResultDecoder;
import com.localhost.tmillner.similartemperature.helpers.Preferences;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private final static String TAG = MainActivity.class.getSimpleName();
    private AutocompleteAdapter acTextViewAdapter= null;
    private GoogleApiClient googleApiClient = null;
    public final static String STORAGE_FILE = String.format(
            "com.localhost.tmillner.similartemperature.%s.RECENT_QUERIES", TAG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Preferences.isFirstTimeUsingApp(this)) {
            Preferences.initialize(this);
        }
        setContentView(R.layout.activity_main);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        googleApiClient.connect();

        AutoCompleteTextView acTextView = (AutoCompleteTextView) findViewById(R.id.userInput);
        acTextViewAdapter = new AutocompleteAdapter(
                this, android.R.layout.simple_dropdown_item_1line);
        acTextViewAdapter.setGoogleApiClient(googleApiClient);
        acTextView.setAdapter(acTextViewAdapter);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        showRecentQueries();
    }


    @Override
    protected void onResume() {
        super.onResume();
        showRecentQueries();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String temperatureMetric = sharedPreferences.getString(
                SettingsActivity.TEMPERATURE_METRIC, getString(
                        R.string.default_settings_temperature_metric_value));
    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected() && googleApiClient != null) {
            acTextViewAdapter.setGoogleApiClient(null);
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    public void getResults(View v) {
        String userInput = ((AutoCompleteTextView) findViewById(R.id.userInput)).getText().toString();
        String fUserInput = userInput.trim();
        PlacesResultDecoder placesResultDecoder = new PlacesResultDecoder(this);
        String country = placesResultDecoder.getCountry(fUserInput);
        if (country == "") {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            country = sharedPreferences.getString(
                    SettingsActivity.DEFAULT_COUNTRY, getString(
                            R.string.default_settings_default_country_value));
        }
        String countryCode = placesResultDecoder.getCountryCode(country);

        WeatherRequest weatherRequest = WeatherRequest.getWeatherRequest(this);
        String zipCode = findZipCode(fUserInput);
        if (zipCode != "") {
            weatherRequest.sendZipDataRequest(this, zipCode, countryCode);
        }
        else {
            String city = placesResultDecoder.getCity(fUserInput);
            if (city != "") {
                weatherRequest.sendLocationDataRequest(this, city, countryCode);
            }
            else {
                String msg = "Invalid input. Enter a postal code or city, country. Like: \n " +
                        " 98103\n Seattle, United States";
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
            }
        }
        weatherRequest = null;
    }

    private List<String> getUserQueries() {
        // String[] recentQueries = {}; Arrays aren't grow-able, prefer using lists instead
        ArrayList<String> recentQueries = new ArrayList<>();
        try {
            FileInputStream inputStream = openFileInput(STORAGE_FILE);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = bufferedReader.readLine()) != null) {
                recentQueries.add(line);
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return recentQueries;
    }

    private void showRecentQueries() {
        List<String> recentQueries = getUserQueries();
        // Although we can reuse the same item layout of results, recently viewed can also
        // utilize a timestamp -- just requires a new layout
        ListView listView = (ListView) findViewById(R.id.recently_viewed);

        listView.setAdapter(new SimpleWeatherResultListAdapter(
                this,
                R.layout.content_weather_result_list_adapter,
                recentQueries));

        if (recentQueries.size() > 0) {
            ImageButton closeButton = (ImageButton) findViewById(R.id.close_button);
            closeButton.setEnabled(true);
        }
        else {
            ImageButton closeButton = (ImageButton) findViewById(R.id.close_button);
            closeButton.setEnabled(false);
        }
    }

    public void clearRecentQueries(View source) {
        deleteFile(STORAGE_FILE);
            ImageButton closeButton = (ImageButton) findViewById(R.id.close_button);
            closeButton.setEnabled(false);
            showRecentQueries();
    }

    private String findZipCode(String input) {
        Pattern p = Pattern.compile("([\\d\\w]{5,})");
        Matcher m = p.matcher(input);
        if (m.find()) {
            return m.group();
        }
        else {
            return "";
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

        if (acTextViewAdapter != null) {
            acTextViewAdapter.setGoogleApiClient(googleApiClient);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
