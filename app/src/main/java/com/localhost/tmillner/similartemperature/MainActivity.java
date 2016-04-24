package com.localhost.tmillner.similartemperature;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;

import com.localhost.tmillner.similartemperature.helpers.Preferences;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private final static String STORAGE_FILE = String.format(
            "com.localhost.tmillner.similartemperature.%s.RECENT_QUERIES", TAG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Preferences.isFirstTimeUsingApp(this)) {
            Preferences.initialize(this);
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
            e.printStackTrace();
        }
        return recentQueries;
    }

    private void showRecentQueries() {
        List<String> recentQueries = getUserQueries();
        Log.i(TAG, recentQueries.toString());
        if (recentQueries.size() > 0) {
            // Although we can reuse the same item layout of results, recently viewed can also
            // utilize a timestamp -- just requires a new layout
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    R.layout.content_weather_result_list_adapter, R.id.result_city, recentQueries);
            ListView listView = (ListView) findViewById(R.id.recently_viewed);
            listView.setAdapter(adapter);
            // No need for on click listener
        }
    }
}
