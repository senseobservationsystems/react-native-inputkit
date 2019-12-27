package nl.sense.rninputkit.inputkit.helper;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by panjiyudasetya on 7/21/17.
 */

public class PreferenceHelper {
    private PreferenceHelper() { }

    private static final String PREFERENCE_NAME = "IK_PREFERENCE";

    /**
     * Add value string into Shared Preference.
     * For non string value, just convert it into {@link String}
     * For an object, use {@link Gson} to stringify the object.
     *
     * @param context current application context
     * @param key     Key preference
     * @param value   Value preference
     */
    public static void add(@NonNull Context context,
                           @NonNull String key,
                           String value) {
        SharedPreferences.Editor editor = context.getSharedPreferences(
                PREFERENCE_NAME,
                Context.MODE_PRIVATE
        ).edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Get value from Shared Preference.
     *
     * @param context current application context
     * @param key     Key preference
     * @return value string.
     */
    public static String get(@NonNull Context context,
                             @NonNull String key) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCE_NAME,
                Context.MODE_PRIVATE
        );
        return preferences.getString(key, null);
    }

    /**
     * Get value from Shared Preference.
     *
     * @param context current application context
     * @param key     Key preference
     * @return {@link JsonObject} jsonify value from share preference.
     */
    public static JsonObject getAsJson(@NonNull Context context,
                                       @NonNull String key) {
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCE_NAME,
                Context.MODE_PRIVATE
        );
        String json = preferences.getString(key, null);
        return TextUtils.isEmpty(json)
                ? new JsonObject()
                : new JsonParser().parse(json).getAsJsonObject();
    }
}
