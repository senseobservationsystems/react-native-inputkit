package nl.sense.rninputkit.inputkit.constant;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This is a constants value to define request read permissions for the given SampleType(s).
 *
 * Created by panjiyudasetya on 6/19/17.
 */

public class SampleType {
    private SampleType() { }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            STEP_COUNT
    })
    public @interface SampleName { }
    public static final String STEP_COUNT = "stepCount";
    public static final String UNAVAILABLE = "unavailable";


    public static final String DISTANCE_WALKING_RUNNING = "distanceWalkingRunning";
    public static final String WEIGHT = "weight";

    public static String checkFitSampleType(@NonNull String sampleType) {
        // Sleep is not supported by GoogleFit at this moment
        if (sampleType.equals(STEP_COUNT) ) {
            return sampleType;
        }
        return UNAVAILABLE;
    }
}
