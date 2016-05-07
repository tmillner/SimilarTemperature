package com.localhost.tmillner.similartemperature.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by macbookpro on 3/23/16.
 */
public class WeatherHelper extends SQLiteOpenHelper {

    private final static String TAG = WeatherHelper.class.getSimpleName();
    public final static String DB_NAME= "db.Weather";
    public final static Integer DB_VERSION= WeatherContract.SCHEMA_REVISION;
    private Context context = null;

    public WeatherHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CountriesContract.CREATE_TABLE);
        populateCountriesData(db, CountriesContract.TABLE);
        db.execSQL(WeatherContract.CREATE_TABLE);
        populateCitiesData(db, WeatherContract.TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WeatherContract.DROP_TABLE);
        db.execSQL(WeatherContract.CREATE_TABLE);
        db.execSQL(CountriesContract.DROP_TABLE);
        db.execSQL(CountriesContract.CREATE_TABLE);
    }

    public void populateCountriesData(SQLiteDatabase db, String table) {
        final String file = "countries.csv";
        InputStreamReader inputStream = null;
        BufferedReader buffer = null;
        try {
            inputStream = new InputStreamReader(this.context.getAssets().open(file));
            buffer = new BufferedReader(inputStream);
            String line;
            while(true) {
                line = buffer.readLine();
                if (line == null || line.equals("")) break;
                String[] csv = line.split(",");
                String countryId = csv[0];
                String countryCode = csv[1];
                String countryName = csv[2];

                ContentValues contentValues = new ContentValues();
                contentValues.put(CountriesContract.COLUMN_ID, countryId);
                contentValues.put(CountriesContract.COLUMN_COUNTRY_CODE, countryCode );
                contentValues.put(CountriesContract.COLUMN_COUNTRY_NAME, countryName );
                db.insert(table, null, contentValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                buffer.close();
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void populateCitiesData(SQLiteDatabase db, String table) {
        final String file = "country_city_population.csv";
        InputStreamReader inputStream = null;
        BufferedReader buffer = null;
        try {
            inputStream = new InputStreamReader(this.context.getAssets().open(file));
            buffer = new BufferedReader(inputStream);
            String line;
            while(true) {
                line = buffer.readLine();
                if (line == null || line.equals("")) break;
                String[] csv = line.split(",");
                String cityId = csv[0];
                String cityName = csv[1];
                String countryId = csv[2];
                String population = csv[3];

                ContentValues contentValues = new ContentValues();
                contentValues.put(WeatherContract.COLUMN_ID, cityId );
                contentValues.put(WeatherContract.COLUMN_CITY, cityName );
                contentValues.put(WeatherContract.COLUMN_COUNTRY_ID, Integer.parseInt(countryId));
                contentValues.put(WeatherContract.COLUMN_POPULATION, population );
                db.insert(table, null, contentValues);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
                buffer.close();
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
