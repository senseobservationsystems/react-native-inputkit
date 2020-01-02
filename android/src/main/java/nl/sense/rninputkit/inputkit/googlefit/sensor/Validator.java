package nl.sense.rninputkit.inputkit.googlefit.sensor;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.OnDataPointListener;

/**
 * Created by panjiyudasetya on 6/15/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class Validator {
    private Validator() { }

    public static void validateDataType(DataType dataType) {
        if (dataType == null) {
            throw new IllegalStateException("Sensor data type must be provided!");
        }
    }

    public static void validateSensorListener(OnDataPointListener listener) {
        if (listener == null) {
            throw new IllegalStateException("Sensor listener must be provided!");
        }
    }
}
