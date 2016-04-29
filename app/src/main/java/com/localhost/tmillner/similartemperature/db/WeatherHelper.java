package com.localhost.tmillner.similartemperature.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.localhost.tmillner.similartemperature.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

/**
 * Created by macbookpro on 3/23/16.
 */
public class WeatherHelper extends SQLiteOpenHelper {

    public final static String DB_NAME= "db.Weather";
    public final static Integer DB_VERSION= WeatherContract.SCHEMA_REVISION;
    private Context context = null;

    public WeatherHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeatherContract.CREATE_TABLE);
        // For previewing, populate w/some data
        db.execSQL(WeatherContract.ADD_SAMPLE_DATA);
        db.execSQL(CountriesContract.CREATE_TABLE);
        populateCountriesData(db, CountriesContract.TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void populateCountriesData(SQLiteDatabase db, String table) {
        InputStream inputStream = null;
        String fileOutput = "";
        try {
            int i;
            inputStream = context.getResources().openRawResource(R.raw.countries);
            while ((i=inputStream.read()) != -1) {
                fileOutput += (char) i;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONObject countries = new JSONObject(fileOutput);
            JSONArray names = countries.names();

            for (int i = 0; i < names.length(); i++) {
                String countryCode = (String) names.get(i);
                String countryName = (String) countries.get(countryCode);

                ContentValues contentValues = new ContentValues();
                contentValues.put(CountriesContract.COLUMN_COUNTRY_CODE, countryCode );
                contentValues.put(CountriesContract.COLUMN_COUNTRY_NAME, countryName );
                db.insert(table, null, contentValues);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
