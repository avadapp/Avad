package com.android.avad.utilities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by sagar_000 on 10/27/2016.
 */
public class Utils {

    public static final String TAG = Utils.class.getSimpleName();

    public static boolean hasLocationPermission(Context context, String fineLocation, String coarseLocation) {

        int res = context.checkCallingOrSelfPermission(fineLocation);

        Log.v(TAG, "permission: " + fineLocation + " = \t\t" +
                (res == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

        int resCoarse = context.checkCallingOrSelfPermission(coarseLocation);

        Log.v(TAG, "permission: " + coarseLocation + " = \t\t" + (resCoarse == PackageManager.PERMISSION_GRANTED ? "GRANTED" : "DENIED"));

        return res == PackageManager.PERMISSION_GRANTED && resCoarse == PackageManager.PERMISSION_GRANTED;

    }
}
