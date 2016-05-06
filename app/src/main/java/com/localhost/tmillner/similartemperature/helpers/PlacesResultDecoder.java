package com.localhost.tmillner.similartemperature.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.localhost.tmillner.similartemperature.db.CountriesContract;
import com.localhost.tmillner.similartemperature.db.WeatherHelper;

/**
 * Created by macbookpro on 4/28/16.
 * This class works on inputs provided by Googles places AutocompletePrediction
 * full text, it typically returns enough detail to gather city and country
 */
public class PlacesResultDecoder {

    private static final String TAG = "PlacesResultDecoder";
    private Context context;

    public PlacesResultDecoder(Context context) {
        this.context = context;
    }

    public String getCity(String locationFullText) {
        String city = "";
        try {
            city = locationFullText.split(",")[0];
            if (city.matches("\\d/g")){
                // City shouldn't have numbers in it
                return "";
            }
        }catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return city;
    }

    public String getCountry(String locationFullText) {
        String country = "";
        if (!country.matches(",") || country.matches("\\d")) {
            return country;
        }
        try {
            String[] addressData = locationFullText.split(",");
            country = addressData[addressData.length -1];
        }catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return country;
    }

    public String getCountryCode(String country) {
        return findCountryCode(country);
    }

    /**
     * Given a full country name ex "united states" return the 2
     * letter country code
     * @param country
     */
    private String findCountryCode(String country) {
        String countryCode = "";
        SQLiteDatabase db = new WeatherHelper(context).getReadableDatabase();
        String[] projection = {
                CountriesContract.COLUMN_COUNTRY_CODE,
                CountriesContract.COLUMN_COUNTRY_NAME
        };

        String whereSelection = CountriesContract.COLUMN_COUNTRY_NAME + " like ?";
        String[] whereArgs= { "%" + country + "%" };

        Cursor cursor = db.query(CountriesContract.TABLE,
                projection,
                whereSelection,
                whereArgs,
                null,
                null,
                null,
                null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            countryCode = cursor.getString(cursor.
                    getColumnIndex(CountriesContract.COLUMN_COUNTRY_CODE));
        }

        return countryCode;
    }
}
