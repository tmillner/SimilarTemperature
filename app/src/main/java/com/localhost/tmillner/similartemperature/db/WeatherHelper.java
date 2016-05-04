package com.localhost.tmillner.similartemperature.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by macbookpro on 3/23/16.
 */
public class WeatherHelper extends SQLiteAssetHelper {

    public final static String DB_NAME= "locations.db";
    public final static Integer DB_VERSION= WeatherContract.SCHEMA_REVISION;

    public WeatherHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public String test() {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String [] sqlSelect = {"country_name"};
        String sqlTables = "countries";

        qb.setTables(sqlTables);
        Cursor c = qb.query(db, sqlSelect, null, null,
                null, null, null);

        c.moveToFirst();
        Log.i("CURSOR", "is " + c);
        return "DONE";
    }
}
