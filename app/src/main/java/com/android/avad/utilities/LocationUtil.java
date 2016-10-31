package com.android.avad.utilities;

import java.util.Locale;

/**
 * Created by sagar_000 on 7/27/2016.
 */
public final class LocationUtil
{
    private LocationUtil() {}


    public static boolean isMetricSystem()
    {
        Locale locale = Locale.getDefault();
        String countryCode = locale.getCountry();
        return (!"US".equals(countryCode) && !"LR".equals(countryCode) && !"MM".equals(countryCode));
    }


    public static int getZoom(int distance)
    {
        if(distance < 50) return 18;
        else if(distance < 100) return 17;
        else if(distance < 500) return 16;
        else if(distance < 1000) return 15;
        else if(distance < 2000) return 14;
        else if(distance < 5000) return 13;
        else if(distance < 10000) return 12;
        else if(distance < 50000) return 11;
        else return 10;
    }

}
