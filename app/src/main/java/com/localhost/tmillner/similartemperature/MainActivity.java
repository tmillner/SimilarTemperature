package com.localhost.tmillner.similartemperature;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.localhost.tmillner.similartemperature.helpers.Preferences;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String STORAGE_FILE = String.format(
            "com.localhost.tmillner.similartemperature.%s.RECENT_QUERIES", TAG);
    private AutocompleteAdapter acTextViewAdapter= null;
    private GoogleApiClient googleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Preferences.isFirstTimeUsingApp(this)) {
            Preferences.initialize(this);
        }
        setContentView(R.layout.activity_main);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .build();
        googleApiClient.connect();
        Log.i(TAG, "Is connected " + googleApiClient.isConnected());

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
                SettingsActivity.TEMPERATURE_METRIC, "Fahrenheit");
        Log.i(TAG, "Metric is " + temperatureMetric);
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
        storeUserQuery(userInput);
        Log.d(TAG, userInput);
        // TODO Allow google places to determine the input filled out locations
        // For now, just send a city
        WeatherRequest.sendLocationDataRequest(this, "test");
    }

    private void storeUserQuery(String query) {
        try {
            FileOutputStream outputStream = openFileOutput(STORAGE_FILE, Context.MODE_APPEND);
            outputStream.write((query + "\n").getBytes());
            outputStream.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
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
            Log.i(TAG, STORAGE_FILE + " does not exist yet! Moving along.");
        }
        return recentQueries;
    }

    private void showRecentQueries() {
        List<String> recentQueries = getUserQueries();
        if (recentQueries.size() > 0) {
            Log.i(TAG, recentQueries.toString());
            // Although we can reuse the same item layout of results, recently viewed can also
            // utilize a timestamp -- just requires a new layout
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.content_weather_result_list_adapter, R.id.result_city, recentQueries);
            ListView listView = (ListView) findViewById(R.id.recently_viewed);
            listView.setAdapter(adapter);
            // No need for on click listener
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
