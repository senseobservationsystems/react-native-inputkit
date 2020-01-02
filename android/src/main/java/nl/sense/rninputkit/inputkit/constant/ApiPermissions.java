package nl.sense.rninputkit.inputkit.constant;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.INTERNET;

/**
 * Created by panjiyudasetya on 7/5/17.
 */

public class ApiPermissions {
    private ApiPermissions() { }

    public static final String[] STEPS_API_PERMISSIONS = {
            INTERNET,
            ACCESS_FINE_LOCATION
    };
}
