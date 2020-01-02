package nl.sense.rninputkit.inputkit.helper;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import static nl.sense.rninputkit.inputkit.constant.RequiredApp.GOOGLE_FIT_PACKAGE_NAME;

/**
 * Created by panjiyudasetya on 9/26/17.
 */

public class AppHelper {
    private AppHelper() { }
    /**
     * Check whether Google Fit application is installed on the device or not.
     * @param context Current application context
     * @return True if installed False otherwise
     */
    public static boolean isGoogleFitInstalled(@NonNull Context context) {
        try {
            context.getPackageManager().getPackageInfo(GOOGLE_FIT_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Check whether Google Play service is up to date or not
     * @param context Current application context
     * @return True if up-to-date, False otherwise
     */
    public static boolean isPlayServiceUpToDate(@NonNull Context context) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        Integer resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context);
        return resultCode != ConnectionResult.SUCCESS ? false : true;
    }

    /**
     * Open required application package in playstore if available
     * @param context Current application context
     * @param packageId Application package id
     */
    public static void openInPlayStore(@NonNull Context context, @NonNull String packageId) {
        final String LINK_TO_GOOGLE_PLAY_SERVICES = "play.google.com/store/apps/details?id=" + packageId + "&hl=en";
        try {
            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://" + LINK_TO_GOOGLE_PLAY_SERVICES)
            ));
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://" + LINK_TO_GOOGLE_PLAY_SERVICES)
            ));
        }
    }

    /**
     * Launch another application in phone
     * @param context Current application context
     * @param packageId Application package id
     */
    public static void openAnotherApp(@NonNull Context context, @NonNull String packageId) {
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageId);
        if (launchIntent != null) {
            context.startActivity(launchIntent);
        }
    }
}
