package com.localhost.tmillner.similartemperature.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by macbookpro on 3/23/16.
 */
public class WeatherHelper extends SQLiteOpenHelper {

    public final static String DB_NAME= "db.Weather";
    public final static Integer DB_VERSION= WeatherContract.SCHEMA_REVISION;

    public WeatherHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WeatherContract.CREATE_TABLE);
        // For previewing, populate w/some data
        db.execSQL(WeatherContract.ADD_SAMPLE_DATA);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
