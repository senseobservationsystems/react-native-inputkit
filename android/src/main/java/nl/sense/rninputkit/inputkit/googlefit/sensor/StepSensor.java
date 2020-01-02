package nl.sense.rninputkit.inputkit.googlefit.sensor;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.OnDataPointListener;

import java.util.concurrent.TimeUnit;

/**
 * Created by panjiyudasetya on 7/20/17.
 */

public class StepSensor extends SensorApi {

    public StepSensor(@NonNull Context context) {
        super(context);
    }

    void setOptions(int samplingRate,
                    @NonNull TimeUnit samplingTimeUnit,
                    @NonNull OnDataPointListener listener) {

        SensorOptions options = new SensorOptions
                .Builder()
                .dataType(DataType.TYPE_STEP_COUNT_DELTA, DataSource.TYPE_DERIVED)
                .samplingRate(samplingRate)
                .samplingTimeUnit(samplingTimeUnit)
                .sensorListener(listener)
                .build();
        setOptions(options);
    }
}
