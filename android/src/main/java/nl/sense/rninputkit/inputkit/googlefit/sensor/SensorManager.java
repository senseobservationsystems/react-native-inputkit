package nl.sense.rninputkit.inputkit.googlefit.sensor;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Pair;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.HealthProvider.SensorListener;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.constant.SampleType.SampleName;
import nl.sense.rninputkit.inputkit.entity.DateContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

import static nl.sense.rninputkit.inputkit.constant.DataSampling.DEFAULT_SAMPLING_TIME_UNIT;
import static nl.sense.rninputkit.inputkit.constant.DataSampling.DEFAULT_TIME_SAMPLING_RATE;

/**
 * Created by panjiyudasetya on 7/24/17.
 */

public class SensorManager {
    private StepSensor mStepSensor;
    private DistanceSensor mDistanceSensor;
    private Context mContext;
    private Map<String, SensorListener<SensorDataPoint>> mSensorListeners;
    // Step tracking data point listener
    private final OnDataPointListener mStepDataPointListener = new OnDataPointListener() {
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            SensorListener<SensorDataPoint> listener = mSensorListeners.get(SampleType.STEP_COUNT);
            if (dataPoint != null && listener != null) {
                listener.onReceive(
                        fromDataPoint(
                                SampleType.STEP_COUNT,
                                dataPoint
                        )
                );
            }
        }
    };
    // Distance walking or running data point listener
    private final OnDataPointListener mDistanceDataPointListener = new OnDataPointListener() {
        @Override
        public void onDataPoint(DataPoint dataPoint) {
            SensorListener<SensorDataPoint> listener = mSensorListeners.get(SampleType.DISTANCE_WALKING_RUNNING);
            if (dataPoint != null && listener != null) {
                listener.onReceive(
                        fromDataPoint(
                                SampleType.DISTANCE_WALKING_RUNNING,
                                dataPoint
                        )
                );
            }
        }
    };

    public SensorManager(@NonNull Context context) {
        mContext = context;
        mStepSensor = new StepSensor(mContext);
        mDistanceSensor = new DistanceSensor(mContext);
        mSensorListeners = new HashMap<>();
    }

    /**
     * Register sensor API listener
     * @param sampleType    sensor type name
     * @param listener      sensor data point listener
     */
    @SuppressWarnings("unused")
    public void registerListener(@NonNull @SampleName String sampleType,
                                 @NonNull SensorListener<SensorDataPoint> listener) {
        mSensorListeners.put(sampleType, listener);
    }

    /**
     * Start sensor tracking Api based on specific sensor.
     * @param sampleType    available sensor
     * @param samplingRate  sensor sampling rate.
     *                      Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                      If sampling rate is unspecified it will be set to 10 minute interval.
     */
    @SuppressWarnings("unused")
    public void startTracking(@NonNull @SampleName String sampleType,
                              @NonNull Pair<Integer, TimeUnit> samplingRate) {
        if (sampleType.equals(SampleType.STEP_COUNT)) {
            startStepTracking(samplingRate);
        } else if (sampleType.equals(SampleType.DISTANCE_WALKING_RUNNING)) {
            startDistanceTracking(samplingRate);
        }
    }

    /**
     * Stop sensor tracking Api based on specific sensor.
     * @param sampleType    available sensor
     */
    @SuppressWarnings("unused")
    public void stopTracking(@NonNull @SampleName String sampleType) {
        if (sampleType.equals(SampleType.STEP_COUNT)) {
            stopStepTracking();
        } else if (sampleType.equals(SampleType.DISTANCE_WALKING_RUNNING)) {
            stopDistanceTracking();
        }
    }

    /**
     * Stop all sensor tracking.
     */
    @SuppressWarnings("unused")
    public void stopTrackingAll(@NonNull final SensorListener<SensorDataPoint> listener) {
        mStepSensor.unsubscribe()
            .continueWithTask(new Continuation<Boolean, Task<Boolean>>() {
                @Override
                public Task<Boolean> then(@NonNull Task<Boolean> task) {
                    return mDistanceSensor.unsubscribe();
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    handleResponse(true,
                            listener,
                            SampleType.STEP_COUNT,
                            SampleType.DISTANCE_WALKING_RUNNING);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    handleResponse(false,
                            listener,
                            SampleType.STEP_COUNT,
                            SampleType.DISTANCE_WALKING_RUNNING);
                }
            });
    }

    private void handleResponse(boolean isSuccess,
                                @NonNull SensorListener<SensorDataPoint> listener,
                                @NonNull String... sensorTypes) {
        String message;
        int status;
        if (isSuccess) {
            status = IKStatus.Code.VALID_REQUEST;
            message = String.format("%s sensor samples has been stopped.",
                    Arrays.toString(sensorTypes));
        } else {
            status = IKStatus.Code.INVALID_REQUEST;
            message = String.format("%s sensor samples has been stopped.",
                    Arrays.toString(sensorTypes));
        }
        listener.onUnsubscribe(new IKResultInfo(status, message));
    }

    /**
     * Helper function to start step count sensor api
     * @param samplingRate sensor sampling rate.
     *                     Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                     If sampling rate is unspecified it will be set to 10 minute interval.
     */
    private void startStepTracking(@NonNull Pair<Integer, TimeUnit> samplingRate) {
        final SensorListener listener = mSensorListeners.get(SampleType.STEP_COUNT);
        if (listener == null) {
            String message = getStartFailureMessage(SampleType.STEP_COUNT);
            throw new IllegalStateException(message);
        }

        int rate = (samplingRate.first == null || samplingRate.first <= 0)
                ? DEFAULT_TIME_SAMPLING_RATE : samplingRate.first;
        TimeUnit timeUnit = samplingRate.second == null
                ? DEFAULT_SAMPLING_TIME_UNIT : samplingRate.second;
        mStepSensor.setOptions(rate, timeUnit, mStepDataPointListener);
        mStepSensor.subscribe(listener);
    }

    /**
     * Helper function to stop step count sensor api
     */
    private void stopStepTracking() {
        final SensorListener listener = mSensorListeners.get(SampleType.STEP_COUNT);
        if (listener == null) {
            String message = getStartFailureMessage(SampleType.STEP_COUNT);
            throw new IllegalStateException(message);
        }

        mStepSensor.unsubscribe(listener);
    }

    /**
     * Helper function to start distance walking or running sensor api
     * @param samplingRate sensor sampling rate.
     *                     Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                     If sampling rate is unspecified it will be set to 10 minute interval.
     */
    private void startDistanceTracking(@NonNull Pair<Integer, TimeUnit> samplingRate) {
        final SensorListener listener = mSensorListeners.get(SampleType.DISTANCE_WALKING_RUNNING);
        if (listener == null) {
            String message = getStartFailureMessage(SampleType.DISTANCE_WALKING_RUNNING);
            throw new IllegalStateException(message);
        }

        int rate = (samplingRate.first == null || samplingRate.first <= 0)
                ? DEFAULT_TIME_SAMPLING_RATE : samplingRate.first;
        TimeUnit timeUnit = samplingRate.second == null
                ? DEFAULT_SAMPLING_TIME_UNIT : samplingRate.second;
        mDistanceSensor.setOptions(rate, timeUnit, mDistanceDataPointListener);

        mDistanceSensor.subscribe(listener);
    }

    /**
     * Helper function to stop distance walking or running sensor api
     */
    private void stopDistanceTracking() {
        final SensorListener listener = mSensorListeners.get(SampleType.DISTANCE_WALKING_RUNNING);
        if (listener == null) {
            String message = getStartFailureMessage(SampleType.DISTANCE_WALKING_RUNNING);
            throw new IllegalStateException(message);
        }

        mDistanceSensor.unsubscribe(listener);
    }

    /**
     * Helper function to generate failure message
     * @param sensorType sensor type name
     * @return failure message
     */
    private String getStartFailureMessage(@NonNull @SampleName String sensorType) {
        return "UNABLE TO PERFORM THIS ACTION!\n"
                + "Please do register sensor listener for " + sensorType
                + " before starting to monitor this event.";
    }

    /**
     * Return payload collections from given data point.
     * @param sensorType sensor type name
     * @param dataPoint Event data point
     * @return {@link SensorDataPoint}
     */
    private static SensorDataPoint fromDataPoint(@NonNull @SampleName String sensorType,
                                                 @NonNull DataPoint dataPoint) {

        List<IKValue<?>> payloads = new ArrayList<>();
        SensorDataPoint output = new SensorDataPoint(
                sensorType,
                Collections.<IKValue<?>>emptyList()
        );

        if (dataPoint.getDataType() == null) return output;

        List<Field> fields = dataPoint.getDataType().getFields();
        if (fields == null || fields.isEmpty()) return output;

        for (Field field : fields) {
            Value value = dataPoint.getValue(field);
            int format = value.getFormat();
            switch (format) {
                case Field.FORMAT_FLOAT:
                    payloads.add(new IKValue<>(
                            value.asFloat(),
                            new DateContent(dataPoint.getStartTime(TimeUnit.MILLISECONDS)),
                            new DateContent(dataPoint.getEndTime(TimeUnit.MILLISECONDS)))
                    );
                    break;
                case Field.FORMAT_INT32:
                    payloads.add(new IKValue<>(
                            value.asInt(),
                            new DateContent(dataPoint.getStartTime(TimeUnit.MILLISECONDS)),
                            new DateContent(dataPoint.getEndTime(TimeUnit.MILLISECONDS)))
                    );
                    break;
                case Field.FORMAT_STRING:
                default:
                    payloads.add(new IKValue<>(
                            value.asString(),
                            new DateContent(dataPoint.getStartTime(TimeUnit.MILLISECONDS)),
                            new DateContent(dataPoint.getEndTime(TimeUnit.MILLISECONDS)))
                    );
                    break;
            }
        }
        output.setPayload(payloads);
        return output;
    }
}
