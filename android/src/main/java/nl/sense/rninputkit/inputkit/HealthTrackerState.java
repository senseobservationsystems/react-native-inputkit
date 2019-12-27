package nl.sense.rninputkit.inputkit;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Pair;

import com.google.gson.JsonObject;

import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.helper.PreferenceHelper;

import static nl.sense.rninputkit.inputkit.constant.SampleType.SampleName;
import static nl.sense.rninputkit.inputkit.constant.SampleType.UNAVAILABLE;
import static nl.sense.rninputkit.inputkit.constant.SampleType.checkFitSampleType;

/**
 * Created by panjiyudasetya on 7/26/17.
 */

public class HealthTrackerState {
    private HealthTrackerState() { }

    /**
     * Stored sensor state in shared preference
     *
     * @param context        Current application context
     * @param stateKey       Tracker state key
     * @param newSensorState New sensor state.
     *                       Sample name should be one of available {@link SampleName}
     *                       If it's unavailable it will throw {@link IllegalStateException}
     */
    public static void save(@NonNull Context context,
                            @NonNull String stateKey,
                            @NonNull Pair<String, Boolean> newSensorState) {
        validateState(newSensorState);

        JsonObject sensorsState = PreferenceHelper.getAsJson(
                context,
                stateKey
        );

        sensorsState.addProperty(
                newSensorState.first,
                newSensorState.second
        );

        // update preference
        PreferenceHelper.add(
                context,
                stateKey,
                sensorsState.toString()
        );
    }

    /**
     * Stored sensor state in shared preference
     *
     * @param context        Current application context
     * @param stateKey       Tracker state key
     * @param enables        New sensor state
     */
    public static void saveAll(@NonNull Context context,
                               @NonNull String stateKey,
                               @NonNull boolean enables) {

        JsonObject sensorsState = PreferenceHelper.getAsJson(
                context,
                stateKey
        );

        sensorsState.addProperty(
                SampleType.STEP_COUNT,
                enables
        );

        sensorsState.addProperty(
                SampleType.DISTANCE_WALKING_RUNNING,
                enables
        );

        // update preference
        PreferenceHelper.add(
                context,
                stateKey,
                sensorsState.toString()
        );
    }

    /**
     * Helper function to validate incoming sensor state.
     * @param state New sensor state.
     *              Sample name should be one of available {@link SampleName}
     *              If it's unavailable it will throw {@link IllegalStateException}
     * @throws IllegalStateException
     */
    private static void validateState(@NonNull Pair<String, Boolean> state)
            throws IllegalStateException {
        @SampleName String sensorSample = state.first;
        if (TextUtils.isEmpty(sensorSample) || checkFitSampleType(sensorSample).equals(UNAVAILABLE)) {
            throw new IllegalStateException("INVALID_SENSOR_SAMPLE_TYPE!");
        }

        if (state.second == null) {
            throw new IllegalStateException("UNSPECIFIED_SENSOR_STATE_VALUE!");
        }
    }
}
