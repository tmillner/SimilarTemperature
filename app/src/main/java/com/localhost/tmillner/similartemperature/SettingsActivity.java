package com.localhost.tmillner.similartemperature;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

public class SettingsActivity extends AppCompatActivity {

    public static String TEMPERATURE_METRIC = "temperature_metric";
    public static String DEFAULT_COUNTRY = "default_country";
    public static String OPEN_WEATHER_API_KEY = "open_weather_api_key";
    public static String MIN_POPULATION_CRITERIA = "min_population_criteria";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getFragmentManager().beginTransaction().replace(
                android.R.id.content, new SettingsFragment()).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
}
