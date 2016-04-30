package com.localhost.tmillner.similartemperature.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.localhost.tmillner.similartemperature.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

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
        db.execSQL(CountriesContract.CREATE_TABLE);
        populateCountriesData(db, CountriesContract.TABLE);
        db.execSQL(WeatherContract.CREATE_TABLE);
        populateCitiesData(db, WeatherContract.TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(WeatherContract.DROP_TABLE);
        db.execSQL(WeatherContract.CREATE_TABLE);
        db.execSQL(WeatherContract.ADD_SAMPLE_DATA);
    }

    public void populateCountriesData(SQLiteDatabase db, String table) {
        String fileOutput = loadFile(R.raw.countries);

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

    public void populateCitiesData(SQLiteDatabase db, String table) {
        String fileOutput = loadFile(R.raw.country_city_population);

        try {
            JSONObject records = new JSONObject(fileOutput);
            JSONArray items = records.names();

            for (int i = 0; i < items.length(); i++) {
                String item = (String) items.get(i);
                JSONObject itemJson = (JSONObject) records.get(item);
                String country = itemJson.getString("country");
                String city = itemJson.getString("city");
                String population = itemJson.getString("population");
                String countryCode = findCountryCode(db, country);

                if (!countryCode.equals("")) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(WeatherContract.COLUMN_CITY, city);
                    contentValues.put(WeatherContract.COLUMN_POPULATION, population);
                    // Might need to be int...
                    contentValues.put(WeatherContract.COLUMN_COUNTRY_ID, countryCode);
                    db.insert(table, null, contentValues);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadFile(int resourceId) {
        InputStream inputStream = null;
        String fileOutput = "";
        try {
            int i;
            inputStream = context.getResources().openRawResource(resourceId);
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
        return fileOutput;
    }

    // See helpers.PlacesResultDecoder
    private String findCountryCode(SQLiteDatabase db, String country) {
        String countryCode = "";
        final String QUERY = "SELECT * FROM " + CountriesContract.TABLE + " +" +
                "WHERE " + CountriesContract.COLUMN_COUNTRY_NAME  + " like %?%";
        Cursor cursor = db.rawQuery(QUERY, new String[]{country});
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            countryCode = cursor.getString(cursor.
                    getColumnIndex(CountriesContract.COLUMN_ID));
        }
        return countryCode;
    }
}
