package com.localhost.tmillner.similartemperature.db;

/**
 * Created by macbookpro on 3/23/16.
 * TODO: Normalize this temp table (foreign keys)
 */
public class WeatherContract {
    public WeatherContract() {
    }

    public static final int SCHEMA_REVISION = 0;
    public static final String TABLE = "weather";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_POPULATION = "population";
    public static final String COLUMN_DATE_POPULATION_NOTED = "date_population_noted";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE + "(" +
            COLUMN_ID + "integer primary key, " +
            COLUMN_CITY + "varchar(200), " +
            COLUMN_COUNTRY + "varchar(150), " +
            COLUMN_POPULATION + "long, " +
            COLUMN_DATE_POPULATION_NOTED + "integer" +
            ")";

    public static final String ADD_SAMPLE_DATA = "INSERT INTO " + TABLE + "(" +
            COLUMN_CITY + ", " +
            COLUMN_COUNTRY + ", " +
            COLUMN_POPULATION + ", " +
            COLUMN_DATE_POPULATION_NOTED + ")" + "VALUES" +
            "(seattle,us,300000,2015)," +
            "(austin,us,200000,2012)," +
            "(chicago,us,400000,2010)," +
            "(chinautla,guatemala,75000,null),";
}
