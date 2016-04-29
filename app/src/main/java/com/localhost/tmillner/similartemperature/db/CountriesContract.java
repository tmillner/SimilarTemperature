package com.localhost.tmillner.similartemperature.db;

/**
 * Created by macbookpro on 4/28/16.
 * TODO: Normalize this temp table (foreign keys)
 */
public class CountriesContract {
    public CountriesContract() {
    }

    public static final int SCHEMA_REVISION = 1;
    public static final String TABLE = "countries";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_COUNTRY_NAME = "country_name";
    public static final String COLUMN_COUNTRY_CODE = "country_code";


    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE + "(" +
            COLUMN_ID + " integer primary key, " +
            COLUMN_COUNTRY_NAME + " varchar(200), " +
            COLUMN_COUNTRY_CODE + " char(3) " +
            ")";

    public static final String JSON_DATA_FILE = "";
}
