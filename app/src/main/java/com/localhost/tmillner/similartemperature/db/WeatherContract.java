package com.localhost.tmillner.similartemperature.db;

/**
 * Created by macbookpro on 3/23/16.
 * TODO: Normalize this temp table (foreign keys)
 */
public class WeatherContract {
    public WeatherContract() {
    }

    public static final int SCHEMA_REVISION = 1;
    public static final String TABLE = "weather";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_COUNTRY_ID = "country_id";
    public static final String COLUMN_POPULATION = "population";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE + "(" +
            COLUMN_ID + " integer primary key, " +
            COLUMN_CITY + " varchar(200), " +
            COLUMN_COUNTRY_ID + " integer, " +
            COLUMN_POPULATION + " long, " +
            "FOREIGN KEY(" + COLUMN_COUNTRY_ID + ") REFERENCES " +
            CountriesContract.TABLE + "(" + CountriesContract.COLUMN_ID + "))";

    public static final String DROP_TABLE = "DROP TABLE " + TABLE +
            " CASCADE CONSTRAINTS";
}
