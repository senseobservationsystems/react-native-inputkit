package nl.sense.rninputkit.inputkit;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.InputKit.Callback;
import nl.sense.rninputkit.inputkit.InputKit.Result;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.constant.SampleType.SampleName;
import nl.sense.rninputkit.inputkit.entity.BloodPressure;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.Weight;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

import static nl.sense.rninputkit.inputkit.constant.IKStatus.Code.IK_NOT_AVAILABLE;
import static nl.sense.rninputkit.inputkit.constant.IKStatus.Code.IK_NOT_CONNECTED;

/**
 * This is a Health contract provider which should be implemented on each Health class variants.
 * Eg : Google Fit, Samsung Health, etc.
 * <p>
 * Make it as an abstract class in case needed to share variable between Health provider
 * <p>
 * Created by panjiyudasetya on 10/13/17.
 */

public abstract class HealthProvider {
    protected static final IKResultInfo UNREACHABLE_CONTEXT = new IKResultInfo(
            IK_NOT_AVAILABLE,
            IKStatus.INPUT_KIT_UNREACHABLE_CONTEXT);
    protected static final IKResultInfo INPUT_KIT_NOT_CONNECTED = new IKResultInfo(
            IK_NOT_CONNECTED,
            String.format(
                    "%s! Make sure to ask request permission before using Input Kit API!",
                    IKStatus.INPUT_KIT_NOT_CONNECTED
            ));
    protected IReleasableHostProvider mReleasableHost;
    private WeakReference<Context> mWeakContext;
    private WeakReference<Activity> mWeakHostActivity;

    public HealthProvider(@NonNull Context context) {
        this.mWeakContext = new WeakReference<>(context.getApplicationContext());
    }

    public HealthProvider(@NonNull Context context, @NonNull IReleasableHostProvider releasableHost) {
        this(context);
        mReleasableHost = releasableHost;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum ProviderType {
        GOOGLE_FIT, SAMSUNG_HEALTH, GARMIN_SDK
    }

    /**
     * Since our health provider bound to weak reference of current application context
     * as well as the activity, then we might need to re-initiate instance class wrapper
     */
    protected interface IReleasableHostProvider {
        /**
         * Release wrapper health provider reference
         */
        void release();
    }

    /**
     * Sensor tracking listener
     *
     * @param <T> Expected data type result
     */
    @SuppressWarnings("SpellCheckingInspection")
    public interface SensorListener<T> {
        void onSubscribe(@NonNull IKResultInfo info);

        void onReceive(@NonNull T data);

        void onUnsubscribe(@NonNull IKResultInfo info);
    }

    /**
     * Get available context.
     *
     * @return {@link Context} current application context.
     * Null will be returned whenever context has no longer available inside
     * of {@link HealthProvider#mWeakContext}
     */
    @Nullable
    public Context getContext() {
        return mWeakContext.get();
    }

    /**
     * Set current host activity.
     * Typically it will be used to show an alert dialog since it bound to the Activity
     *
     * @param activity Current Host activity
     */
    public void setHostActivity(@Nullable Activity activity) {
        mWeakHostActivity = new WeakReference<>(activity);
    }

    /**
     * Get available host activity.
     *
     * @return {@link Activity} current host activity.
     * Null will be returned whenever host activity has no longer available inside
     * of {@link HealthProvider#mWeakHostActivity}
     */
    @Nullable
    public Activity getHostActivity() {
        return mWeakHostActivity == null ? null : mWeakHostActivity.get();
    }

    /**
     * Handler function when application context no longer available
     */
    protected void onUnreachableContext() {
        if (mReleasableHost != null) mReleasableHost.release();
    }

    /**
     * Handler function when application context no longer available
     *
     * @param callback {@link Callback} listener
     */
    protected void onUnreachableContext(@NonNull Callback callback) {
        callback.onNotAvailable(UNREACHABLE_CONTEXT);
        if (mReleasableHost != null) mReleasableHost.release();
    }

    /**
     * Handler function when application context no longer available
     *
     * @param callback {@link Result} listener
     */
    protected void onUnreachableContext(@NonNull Result callback) {
        callback.onError(UNREACHABLE_CONTEXT);
        if (mReleasableHost != null) mReleasableHost.release();
    }

    /**
     * Call {@link Result#onError(IKResultInfo)} whenever Health provider is not connected.
     *
     * @param callback {@link Result} callback which to be handled
     * @return True if available, False otherwise
     */
    protected boolean isAvailable(@NonNull Result callback) {
        if (!isAvailable()) {
            callback.onError(INPUT_KIT_NOT_CONNECTED);
            return false;
        }
        return true;
    }

    /**
     * Check Health provider availability.
     *
     * @return True if health provider is available, False otherwise.
     */
    public abstract boolean isAvailable();

    /**
     * Check permission status of specific sensor in Health provider.
     * @param permissionTypes permission types of sensor that needs to be check
     * @return True if health provider is available, False otherwise.
     */
    public abstract boolean isPermissionsAuthorised(String[] permissionTypes);

    /**
     * Authorize Health provider connection.
     *
     * @param callback       {@link Callback} event listener
     * @param permissionType permission type. in case specific handler required when asking input kit
     *                       type.
     */
    public abstract void authorize(@NonNull Callback callback, String... permissionType);

    /**
     * Disconnect from Health provider
     *
     * @param callback {@link Result} event listener
     */
    public abstract void disconnect(@NonNull Result<Boolean> callback);

    /**
     * Get total distance of walk on specific time range.
     *
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param limit     historical data limitation
     *                  set to null if you want to calculate all available distance within specific range
     * @param callback  {@link Result<Float>} containing number of total distance
     */
    public abstract void getDistance(long startTime,
                                     long endTime,
                                     int limit,
                                     @NonNull Result<Float> callback);

    /**
     * Get sample distance within specific time range.
     *
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param limit     historical data limitation
     *                  set to null if you want to calculate all available distance within specific range
     * @param callback  {@link Result<Float>} containing number of total distance
     */
    public abstract void getDistanceSamples(long startTime,
                                            long endTime,
                                            int limit,
                                            @NonNull Result<List<IKValue<Float>>> callback);

    /**
     * Get total Today steps count.
     *
     * @param callback {@link Result<Integer>} containing number of total steps count
     */
    public abstract void getStepCount(@NonNull Result<Integer> callback);

    /**
     * Get total steps count of specific range
     *
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param callback  {@link Result<Integer>} containing number of total steps count
     */
    public abstract void getStepCount(long startTime,
                                      long endTime,
                                      int limit,
                                      @NonNull Result<Integer> callback);

    /**
     * Return data distribution of step count value through out a specific range.
     *
     * @param startTime epoch for the start date of the range where the distribution should be calculated from.
     * @param endTime   epoch for the end date of the range where the distribution should be calculated from.
     * @param interval  Interval
     * @param limit     historical data limitation
     *                  set to null if you want to calculate all available distance within specific range
     * @param callback  {@link Result<StepContent>} Steps content set if available.
     **/
    public abstract void getStepCountDistribution(long startTime,
                                                  long endTime,
                                                  @NonNull @Interval.IntervalName String interval,
                                                  int limit,
                                                  @NonNull Result<StepContent> callback);

    /**
     * Returns data contains sleep analysis data of a specific range. Sorted recent data first.
     *
     * @param startTime epoch for the start date of the range
     * @param endTime   epoch for the end date of the range
     * @param callback  {@link Result} containing a set of sleep analysis samples
     */
    // TODO: Define data type of sleep analysis samples Input Kit result
    public abstract void getSleepAnalysisSamples(long startTime,
                                                 long endTime,
                                                 @NonNull InputKit.Result<List<IKValue<String>>> callback);

    /**
     * Get blood pressure history
     *
     * @param startTime epoch for the start date of the range
     * @param endTime   epoch for the end date of the range
     * @param callback  {@link Result} containing a set history of user blood pressure
     */
    public abstract void getBloodPressure(long startTime,
                                          long endTime,
                                          @NonNull Result<List<BloodPressure>> callback);

    /**
     * Get blood weight history
     *
     * @param startTime epoch for the start date of the range
     * @param endTime   epoch for the end date of the range
     * @param callback  {@link Result} containing a set history of user weight
     */
    public abstract void getWeight(long startTime,
                                   long endTime,
                                   @NonNull Result<List<Weight>> callback);

    /**
     * Start monitoring health sensors.
     *
     * @param sensorType   sensor type should be one of these {@link SampleName} sensor
     * @param samplingRate sensor sampling rate.
     *                     Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                     If sampling rate is unspecified it will be set to 10 minute interval.
     * @param listener     {@link SensorListener} sensor listener
     */
    public abstract void startMonitoring(@NonNull @SampleName String sensorType,
                                         @NonNull Pair<Integer, TimeUnit> samplingRate,
                                         @NonNull SensorListener<SensorDataPoint> listener);

    /**
     * Stop monitoring health sensors.
     *
     * @param sensorType Sensor type should be one of these {@link SampleName} sensor
     * @param listener   {@link SensorListener} sensor listener
     */
    public abstract void stopMonitoring(@NonNull @SampleName String sensorType,
                                        @NonNull SensorListener<SensorDataPoint> listener);

    /**
     * Start tracking specific sensor.
     *
     * @param sensorType   Sample type should be one of these {@link SampleName} sensor
     * @param samplingRate sensor sampling rate.
     *                     Sensor will be started every X-Time Unit, for instance : { 5, {@link TimeUnit#MINUTES} }.
     *                     If sampling rate is unspecified it will be set to 10 minute interval.
     * @param listener     {@link SensorListener} sensor listener
     */
    public abstract void startTracking(@NonNull @SampleName String sensorType,
                                       @NonNull Pair<Integer, TimeUnit> samplingRate,
                                       @NonNull SensorListener<SensorDataPoint> listener);

    /**
     * Stop tracking specific sensor.
     *
     * @param sensorType Sample type should be one of these {@link SampleName} sensor
     * @param listener   {@link SensorListener} sensor listener
     */
    public abstract void stopTracking(@NonNull String sensorType,
                                      @NonNull SensorListener<SensorDataPoint> listener);

    /**
     * Stop all tracking specific sensor.
     *
     * @param listener {@link SensorListener} sensor listener
     */
    public abstract void stopTrackingAll(@NonNull SensorListener<SensorDataPoint> listener);
}
