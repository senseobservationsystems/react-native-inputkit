package nl.sense.rninputkit.inputkit;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.HealthProvider.IReleasableHostProvider;
import nl.sense.rninputkit.inputkit.HealthProvider.ProviderType;
import nl.sense.rninputkit.inputkit.HealthProvider.SensorListener;
import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.entity.BloodPressure;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.Weight;
import nl.sense.rninputkit.inputkit.shealth.SamsungHealthProvider;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;
import nl.sense.rninputkit.inputkit.status.IKProviderInfo;
import nl.sense.rninputkit.inputkit.googlefit.GoogleFitHealthProvider;

/**
 * Created by panjiyudasetya on 6/14/17.
 */

public class InputKit implements IReleasableHostProvider {
    private static InputKit sInputKit;
    private HealthProvider mCurrentHealthProvider;
    private GoogleFitHealthProvider mGoogleFitHealthProvider;
    private SamsungHealthProvider mSamsungHealthProvider;

    /**
     * A callback result for each Input Kit functionality.
     *
     * @param <T> Expected result.
     */
    public interface Result<T> {
        /**
         * Callback function to handle new available data
         *
         * @param data Expected data
         */
        void onNewData(T data);

        /**
         * Callback function to handle any exceptions if any
         *
         * @param error {@link IKResultInfo}
         */
        void onError(@NonNull IKResultInfo error);
    }

    public interface Callback {
        /**
         * This action will be triggered when successfully connected to Input Kit Service.
         * @param addMessages additional message
         */
        void onAvailable(String... addMessages);

        /**
         * This event will be triggered when Input Kit is not available for some reason.
         * @param reason Typically contains error code and error message.
         */
        void onNotAvailable(@NonNull IKResultInfo reason);

        /**
         * This event will be triggered whenever connection to Input Kit service has been rejected.
         * In any case, the problem probably solved by call
         * {@link com.google.android.gms.common.ConnectionResult#startResolutionForResult(Activity, int)}
         * which int value should be referred to
         * {@link com.google.android.gms.common.ConnectionResult#getErrorCode()}.
         * But this action required UI interaction, so be careful with it.
         * @param connectionError {@link IKProviderInfo}
         */
        void onConnectionRefused(@NonNull IKProviderInfo connectionError);
    }

    private InputKit(@NonNull Context context) {
        mGoogleFitHealthProvider = new GoogleFitHealthProvider(context, this);
        mSamsungHealthProvider = new SamsungHealthProvider(context);

        // By default it will use Google Fit Health provider
        mCurrentHealthProvider = mGoogleFitHealthProvider;
    }

    /**
     * Get instance of Input Kit class.
     *
     * @param context current application context
     * @return {@link InputKit}
     */
    public static InputKit getInstance(@NonNull Context context) {
        if (sInputKit == null) sInputKit = new InputKit(context);
        return sInputKit;
    }

    /**
     * Set current host activity.
     * Typically it will be used to show an alert dialog since it bound to the Activity
     *
     * @param activity  Current Host activity
     */
    @SuppressWarnings("unused")//This is a public API
    public void setHostActivity(@Nullable Activity activity) {
        mCurrentHealthProvider.setHostActivity(activity);
    }

    /**
     * Set priority health provider
     * @param healthProvider available health provider
     */
    @SuppressWarnings("unused")//This is a public API
    public void setHealthProvider(@NonNull ProviderType healthProvider) {
        switch (healthProvider) {
            case GOOGLE_FIT: mCurrentHealthProvider = mGoogleFitHealthProvider;
                break;
            case SAMSUNG_HEALTH:
                mCurrentHealthProvider = mSamsungHealthProvider;
                break;
            case GARMIN_SDK:
            default:
                mCurrentHealthProvider = mGoogleFitHealthProvider;
                break;
        }
    }

    /**
     * Authorize Input Kit service connections.
     * @param callback       event listener
     * @param permissionType permission type. in case specific handler required when asking input kit
     *                       type.
     *
     */
    @SuppressWarnings("unused")//This is a public API
    public void authorize(@NonNull Callback callback, String... permissionType) {
        mCurrentHealthProvider.authorize(callback, permissionType);
    }

    /**
     * Disconnect from current Health provider
     * @param callback {@link Result} event listener
     */
    @SuppressWarnings("unused")//This is a public API
    public void disconnectCurrentHealthProvider(@NonNull Result<Boolean> callback) {
        mCurrentHealthProvider.disconnect(callback);
    }

    /**
     * Check health availability.
     */
    @SuppressWarnings("unused")//This is a public API
    public boolean isAvailable() {
        return mCurrentHealthProvider.isAvailable();
    }

    /**
     * Check authorised permission type availability.
     * @param permissionType requested permission type
     */
    @SuppressWarnings("unused")//This is a public API
    public boolean isPermissionsAuthorised(String[] permissionType) {
        return mCurrentHealthProvider.isPermissionsAuthorised(permissionType);
    }

    /**
     * Get total distance of walk on specific time range.
     *
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param limit     historical data limitation
     *                  set to 0 if you want to calculate all available distance within specific range
     * @param callback {@link Result <Float>} containing number of total distance
     */
    @SuppressWarnings("unused")//This is a public API
    public void getDistance(long startTime, long endTime, int limit, @NonNull Result<Float> callback) {
        mCurrentHealthProvider.getDistance(startTime, endTime, limit, callback);
    }

    /**
     * Get sample distance of walk on specific time range.
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param limit     historical data limitation
     *                  set to 0 if you want to calculate all available distance within specific range
     * @param callback {@link Result} containing set of available distance
     */
    @SuppressWarnings("unused")//This is a public API
    public void getDistanceSamples(long startTime,
                                   long endTime,
                                   int limit,
                                   @NonNull Result<List<IKValue<Float>>> callback) {
        mCurrentHealthProvider.getDistanceSamples(startTime, endTime, limit, callback);
    }

    /**
     * Get total Today steps count.
     * @param callback {@link Result <Integer>} containing number of total steps count
     */
    @SuppressWarnings("unused")//This is a public API
    public void getStepCount(@NonNull Result<Integer> callback) {
        mCurrentHealthProvider.getStepCount(callback);
    }

    /**
     * Get total steps count of specific range.
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param limit     historical data limitation
     *                  set to 0 if you want to calculate all available distance within specific range
     * @param callback {@link Result <Integer>} containing number of total steps count
     */
    @SuppressWarnings("unused")//This is a public API
    public void getStepCount(long startTime,
                             long endTime,
                             int limit,
                             @NonNull Result<Integer> callback) {
        mCurrentHealthProvider.getStepCount(startTime, endTime, limit, callback);
    }

    /**
     * Get distribution step count history by specific time period.
     * This function should be called within asynchronous process because of
     * reading historical data through {@link com.google.android.gms.fitness.Fitness#HistoryApi} will be executed on main
     * thread by default.
     *
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param interval  on of any {@link nl.sense.rninputkit.inputkit.constant.Interval.IntervalName}
     * @param limit     historical data limitation
     *                  set to null if you want to calculate all available step count within specific range
     * @param callback {@link Result <StepContent>} containing a set of history step content
     */
    @SuppressWarnings("unused")//This is a public API
    public void getStepCountDistribution(long startTime,
                                         long endTime,
                                         @NonNull @Interval.IntervalName String interval,
                                         int limit,
                                         @NonNull Result<StepContent> callback) {
        mCurrentHealthProvider.getStepCountDistribution(startTime, endTime, interval, limit, callback);
    }

    /**
     * Returns data contains sleep analysis data of a specific range. Sorted recent data first.
     * @param startTime epoch for the start date of the range
     * @param endTime   epoch for the end date of the range
     * @param callback  {@link Result} containing a set of sleep analysis samples
     */
    public void getSleepAnalysisSamples(long startTime, long endTime,
                                        @NonNull InputKit.Result<List<IKValue<String>>> callback) {
        mCurrentHealthProvider.getSleepAnalysisSamples(startTime, endTime, callback);
    }

    /**
     * Get blood pressure history
     * @param startTime epoch for the start date of the range
     * @param endTime   epoch for the end date of the range
     * @param callback  {@link Result} containing a set history of user blood pressure
     */
    public void getBloodPressure(long startTime, long endTime, @NonNull Result<List<BloodPressure>> callback) {
        mCurrentHealthProvider.getBloodPressure(startTime, endTime, callback);
    }

    /**
     * Get weight history
     * @param startTime epoch for the start date of the range
     * @param endTime   epoch for the end date of the range
     * @param callback  {@link Result} containing a set history of user weight
     */
    public void getWeight(long startTime, long endTime, @NonNull Result<List<Weight>> callback) {
        mCurrentHealthProvider.getWeight(startTime, endTime, callback);
    }

    /* Start monitoring health sensors.
     * @param sensorType     sensor type should be one of these {@link SampleType.SampleName} sensor
     * @param samplingRate   sensor sampling rate.
     *                       Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                       If sampling rate is unspecified it will be set to 10 minute interval.
     * @param listener       {@link SensorListener} sensor listener
     */
    @SuppressWarnings("unused")//This is a public API
    public void startMonitoring(@NonNull String sensorType,
                                @NonNull Pair<Integer, TimeUnit> samplingRate,
                                @NonNull SensorListener<SensorDataPoint> listener) {
        mCurrentHealthProvider.startMonitoring(sensorType, samplingRate, listener);
    }

    /**
     * Stop monitoring health sensors.
     * @param sensorType Sensor type should be one of these {@link SampleType.SampleName} sensor
     * @param listener   {@link SensorListener} sensor listener
     */
    @SuppressWarnings("unused")//This is a public API
    public void stopMonitoring(@NonNull String sensorType,
                               @NonNull SensorListener<SensorDataPoint> listener) {
        mCurrentHealthProvider.stopMonitoring(sensorType, listener);
    }

    /**
     * Start tracking specific sensor.
     *
     * @param sensorType   Sample type should be one of these {@link SampleType.SampleName} sensor
     * @param samplingRate sensor sampling rate.
     *                     Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                     If sampling rate is unspecified it will be set to 10 minute interval.
     * @param listener     {@link SensorListener} sensor listener
     */
    @SuppressWarnings("unused")//This is a public API
    public void startTracking(@NonNull @SampleType.SampleName String sensorType,
                              @NonNull Pair<Integer, TimeUnit> samplingRate,
                              @NonNull SensorListener<SensorDataPoint> listener) {
        mCurrentHealthProvider.startTracking(sensorType, samplingRate, listener);
    }

    /**
     * Stop tracking specific sensor.
     *
     * @param sensorType Sample type should be one of these {@link SampleType.SampleName} sensor
     * @param listener   {@link SensorListener} sensor listener
     */
    @SuppressWarnings("unused")//This is a public API
    public void stopTracking(@NonNull String sensorType,
                             @NonNull SensorListener<SensorDataPoint> listener) {
        mCurrentHealthProvider.stopTracking(sensorType, listener);
    }

    /**
     * Stop all tracking specific sensor.
     *
     * @param listener {@link SensorListener} sensor listener
     */
    @SuppressWarnings("unused")//This is a public API
    public void stopTrackingAll(@NonNull SensorListener<SensorDataPoint> listener) {
        mCurrentHealthProvider.stopTrackingAll(listener);
    }

    @Override
    public void release() {
        sInputKit = null;
        mCurrentHealthProvider = null;
        mGoogleFitHealthProvider = null;
        mSamsungHealthProvider = null;
        // TODO: Put another references which should be released.
    }
}
