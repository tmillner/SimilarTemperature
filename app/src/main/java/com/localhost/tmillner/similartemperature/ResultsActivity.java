package com.localhost.tmillner.similartemperature;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.localhost.tmillner.similartemperature.db.WeatherContract;
import com.localhost.tmillner.similartemperature.db.WeatherHelper;
import com.localhost.tmillner.similartemperature.helpers.WeatherRequest;

public class ResultsActivity extends AppCompatActivity {

    private String degrees;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
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
        this.setDegrees();
        this.populateMatchesList();
    }

    public void setDegrees() {
        Intent intent = getIntent();
        this.degrees = intent.getStringExtra(WeatherRequest.WEATHER_CURRENT);
    }

    public void populateMatchesList() {
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
        while(cursor.moveToNext()) {
            String city = cursor.getString(cursor.getColumnIndex(WeatherContract.COLUMN_CITY));
            String country = cursor.getString(cursor.getColumnIndex(WeatherContract.COLUMN_COUNTRY));

        }
    }
}
