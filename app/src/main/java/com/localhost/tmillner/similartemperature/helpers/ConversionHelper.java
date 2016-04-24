package com.localhost.tmillner.similartemperature.helpers;

import java.text.DecimalFormat;

/**
 * Created by macbookpro on 4/23/16.
 */
public class ConversionHelper {

    public static Double fahrenheitToCelsius(Double degrees) {
        String fDegrees = new DecimalFormat("#.00").format(((degrees - 32)/1.8));
        return Double.parseDouble(fDegrees);
    }

    public static Double celsiusToFahrenheit(Double degrees) {
        String fDegrees = new DecimalFormat("#.00").format(((degrees*1.8)+32));
        return Double.parseDouble(fDegrees);
    }
}
