package nl.sense.rninputkit.inputkit.googlefit;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.HealthProvider;
import nl.sense.rninputkit.inputkit.HealthTrackerState;
import nl.sense.rninputkit.inputkit.InputKit.Callback;
import nl.sense.rninputkit.inputkit.InputKit.Result;
import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.constant.Constant;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.googlefit.history.FitHistory;
import nl.sense.rninputkit.inputkit.googlefit.sensor.SensorManager;
import nl.sense.rninputkit.inputkit.helper.AppHelper;
import nl.sense.rninputkit.inputkit.helper.InputKitTimeUtils;
import nl.sense.rninputkit.inputkit.status.IKProviderInfo;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by panjiyudasetya on 10/13/17.
 */

public class GoogleFitHealthProvider extends HealthProvider {
    public static final int GF_PERMISSION_REQUEST_CODE = 77;
    private static final IKResultInfo OUT_OF_DATE_PLAY_SERVICE = new IKResultInfo(
            IKStatus.Code.OUT_OF_DATE_PLAY_SERVICE,
            IKStatus.INPUT_KIT_OUT_OF_DATE_PLAY_SERVICE
    );
    private static final IKResultInfo REQUIRED_GOOGLE_FIT_APP = new IKResultInfo(
            IKStatus.Code.GOOGLE_FIT_REQUIRED,
            IKStatus.REQUIRED_GOOGLE_FIT_APP
    );
    private static final String TAG = GoogleFitHealthProvider.class.getSimpleName();
    private FitHistory mFitHistory;
    private SensorManager mSensorMonitoring;
    private SensorManager mSensorTracking;

    public GoogleFitHealthProvider(@NonNull Context context) {
        super(context);
        init(context);
    }

    public GoogleFitHealthProvider(@NonNull Context context, @NonNull IReleasableHostProvider releasableHost) {
        super(context, releasableHost);
        init(context);
    }

    private void init(@NonNull Context context) {
        mFitHistory = new FitHistory(context);
        mSensorMonitoring = new SensorManager(context);
        mSensorTracking = new SensorManager(context);
    }

    @Override
    public boolean isAvailable() {
        return getContext() != null && GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getContext()));
    }

    @Override
    public boolean isPermissionsAuthorised(String[] permissionTypes) {
        if (getContext() != null && permissionTypes != null && permissionTypes.length > 0) {
            FitnessOptions options = FitPermissionSet.getInstance().getPermissionsSet(permissionTypes);
            return GoogleSignIn.hasPermissions(
                        GoogleSignIn.getLastSignedInAccount(getContext()),
                        options);
        }
        return isAvailable();
    }

    @Override
    public void authorize(@NonNull final Callback callback, String... permissionType) {
        Context context = getContext();
        if (context == null) {
            onUnreachableContext(callback);
            return;
        }

        if (!AppHelper.isPlayServiceUpToDate(context)) {
            callback.onNotAvailable(OUT_OF_DATE_PLAY_SERVICE);
            return;
        }

        if (!AppHelper.isGoogleFitInstalled(context)) {
            callback.onNotAvailable(REQUIRED_GOOGLE_FIT_APP);
            return;
        }

        if (!isPermissionsAuthorised(permissionType)) {
            if (getHostActivity() == null) {
                onUnreachableContext(callback);
                return;
            }

            startSignedInAndAskForPermission(permissionType);

            callback.onConnectionRefused(new IKProviderInfo(
                    IKStatus.Code.REQUIRED_GRANTED_PERMISSIONS,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        callback.onAvailable("CONNECTED_TO_GOOGLE_FIT");
    }

    @Override
    public void disconnect(@NonNull final Result<Boolean> callback) {
        final Context context = getContext();
        if (isInvalidContext(context, callback)) return;
        if (!isAvailable(callback)) return;

        assert context != null;
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            // Disconnect from Fit App and revoke existing permission access.
            Fitness.getConfigClient(context, account).disableFit()
                    .continueWithTask(new Continuation<Void, Task<Void>>() {
                        @Override
                        public Task<Void> then(@NonNull Task<Void> task) {
                            return GoogleSignIn.getClient(context, getOptions())
                                    .revokeAccess();
                        }
                    })
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            callback.onNewData(true);
                        }
                    });
        }
    }

    @Override
    public void getStepCount(@NonNull final Result<Integer> callback) {
        Context context = getContext();
        if (isInvalidContext(context, callback)) return;
        if (!isAvailable(callback)) return;

        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                mFitHistory.getStepCount(callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, SampleType.STEP_COUNT);
    }

    @Override
    public void getStepCount(final long startTime,
                             final long endTime,
                             final int limit,
                             @NonNull final Result<Integer> callback) {
        if (isInvalidContext(getContext(), callback)) return;
        if (!isAvailable(callback)) return;
        if (!InputKitTimeUtils.validateTimeInput(startTime, endTime, callback)) return;

        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                Options.Builder builder = new Options.Builder()
                        .startTime(startTime)
                        .endTime(endTime)
                        .limitation(limit <= 0 ? DataReadRequest.NO_LIMIT : limit);
                // Guard the aggregation data. If limit is not specified then we need to use
                // data aggregation to optimize query performance.
                if (limit <= 0) builder.useDataAggregation();
                Options options = builder.build();
                mFitHistory.getStepCount(options, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, SampleType.STEP_COUNT);
    }

    @Override
    public void getStepCountDistribution(final long startTime,
                                         final long endTime,
                                         @NonNull @Interval.IntervalName final String interval,
                                         final int limit,
                                         @NonNull final Result<StepContent> callback) {
        if (isInvalidContext(getContext(), callback)) return;
        if (!isAvailable(callback)) return;
        if (!InputKitTimeUtils.validateTimeInput(startTime, endTime, callback)) return;

        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                TimeInterval timeInterval = new TimeInterval(interval);
                Options.Builder builder = new Options.Builder()
                        .startTime(startTime)
                        .endTime(endTime)
                        .timeInterval(timeInterval)
                        .limitation(limit <= 0 ? DataReadRequest.NO_LIMIT : limit);
                // Guard the aggregation data. If limit is not specified then we need to use
                // data aggregation to optimize query performance.
                if (limit <= 0) builder.useDataAggregation();
                Options options = builder.build();
                mFitHistory.getStepCountDistribution(options, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, SampleType.STEP_COUNT);
    }

    @Override
    public void startMonitoring(@NonNull @SampleType.SampleName final String sensorType,
                                @NonNull final Pair<Integer, TimeUnit> samplingRate,
                                @NonNull final SensorListener<SensorDataPoint> listener) {
        final Context context = getContext();
        if (isInvalidContext(context, listener, true)) return;
        if (isSensorTypeUnavailable(sensorType, listener)) return;

        assert context != null;
        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                mSensorMonitoring.registerListener(sensorType, new SensorListener<SensorDataPoint>() {
                    @Override
                    public void onSubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.MONITORED_HEALTH_SENSORS,
                                    Pair.create(sensorType, true)
                            );
                        }
                        listener.onSubscribe(info);
                    }

                    @Override
                    public void onReceive(@NonNull SensorDataPoint data) {
                        listener.onReceive(data);
                    }

                    @Override
                    public void onUnsubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.MONITORED_HEALTH_SENSORS,
                                    Pair.create(sensorType, false)
                            );
                        }
                        listener.onUnsubscribe(info);
                    }
                });
                mSensorMonitoring.startTracking(sensorType, samplingRate);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onSubscribe(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, sensorType);
    }

    @Override
    public void stopMonitoring(@NonNull @SampleType.SampleName final String sensorType,
                               @NonNull final SensorListener<SensorDataPoint> listener) {
        final Context context = getContext();
        if (isInvalidContext(context, listener, false)) return;
        if (isSensorTypeUnavailable(sensorType, listener)) return;

        assert context != null;
        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                mSensorMonitoring.registerListener(sensorType, new SensorListener<SensorDataPoint>() {
                    @Override
                    public void onSubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.MONITORED_HEALTH_SENSORS,
                                    Pair.create(sensorType, true)
                            );
                        }
                        listener.onSubscribe(info);
                    }

                    @Override
                    public void onReceive(@NonNull SensorDataPoint data) {
                        listener.onReceive(data);
                    }

                    @Override
                    public void onUnsubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.MONITORED_HEALTH_SENSORS,
                                    Pair.create(sensorType, false)
                            );
                        }
                        listener.onUnsubscribe(info);
                    }
                });
                mSensorMonitoring.stopTracking(sensorType);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onUnsubscribe(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, sensorType);
    }

    @Override
    public void startTracking(@NonNull @SampleType.SampleName final String sensorType,
                              @NonNull final Pair<Integer, TimeUnit> samplingRate,
                              @NonNull final SensorListener<SensorDataPoint> listener) {
        final Context context = getContext();
        if (isInvalidContext(context, listener, true)) return;
        if (isSensorTypeUnavailable(sensorType, listener)) return;
        if (!isAvailable()) {
            listener.onSubscribe(INPUT_KIT_NOT_CONNECTED);
            return;
        }

        assert context != null;
        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                mSensorTracking.registerListener(sensorType, new SensorListener<SensorDataPoint>() {
                    @Override
                    public void onSubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.TRACKED_HEALTH_SENSORS,
                                    Pair.create(sensorType, true)
                            );
                        }
                        listener.onSubscribe(info);
                    }

                    @Override
                    public void onReceive(@NonNull SensorDataPoint data) {
                        listener.onReceive(data);
                    }

                    @Override
                    public void onUnsubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.TRACKED_HEALTH_SENSORS,
                                    Pair.create(sensorType, false)
                            );
                        }
                        listener.onUnsubscribe(info);
                    }
                });
                mSensorTracking.startTracking(sensorType, samplingRate);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onSubscribe(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, sensorType);
    }

    @Override
    public void stopTracking(@NonNull final String sensorType,
                             @NonNull final SensorListener<SensorDataPoint> listener) {
        final Context context = getContext();
        if (isInvalidContext(context, listener, false)) return;
        if (isSensorTypeUnavailable(sensorType, listener)) return;
        if (!isAvailable()) {
            listener.onUnsubscribe(INPUT_KIT_NOT_CONNECTED);
            return;
        }

        assert context != null;
        callWithValidToken(new AccessTokenListener() {
            @Override
            public void onSuccess() {
                mSensorTracking.registerListener(sensorType, new SensorListener<SensorDataPoint>() {
                    @Override
                    public void onSubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.TRACKED_HEALTH_SENSORS,
                                    Pair.create(sensorType, true)
                            );
                        }
                        listener.onSubscribe(info);
                    }

                    @Override
                    public void onReceive(@NonNull SensorDataPoint data) {
                        listener.onReceive(data);
                    }

                    @Override
                    public void onUnsubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            HealthTrackerState.save(
                                    context,
                                    Constant.TRACKED_HEALTH_SENSORS,
                                    Pair.create(sensorType, false)
                            );
                        }
                        listener.onUnsubscribe(info);
                    }
                });
                mSensorTracking.stopTracking(sensorType);
            }

            @Override
            public void onFailure(Exception e) {
                listener.onUnsubscribe(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                        e.getMessage()));
            }
        }, sensorType);
    }


    /**
     * Helper function to check whether sensor type is available or not.
     * @param sensorType    Sensor type
     * @param listener      {@link SensorListener}
     * @return True if available, False otherwise.
     */
    private boolean isSensorTypeUnavailable(@NonNull String sensorType, @NonNull SensorListener listener) {
        if (!SampleType.checkFitSampleType(sensorType).equals(SampleType.UNAVAILABLE)) return false;
        listener.onSubscribe(
                new IKResultInfo(
                        IKStatus.Code.INVALID_REQUEST,
                        sensorType + " : SENSOR_TYPE_IS_NOT_AVAILABLE!"
                )
        );
        return true;
    }

    /**
     * Check either context is still valid or not.
     * @param context   Current application context.
     * @param callback  Result callback
     * @return          True if context is valid, False otherwise.
     */
    private boolean isInvalidContext(@Nullable Context context,
                                     @NonNull Result callback) {
        if (context != null) return false;

        onUnreachableContext(callback);
        return true;
    }

    /**
     *
     * Check either context is still valid or not.
     * @param context   Current application context.
     * @param listener  Sensor listener.
     * @param isSubscribeAction Set to true if it's coming from subscriptions, False otherwise.
     * @return True if context is valid, False otherwise.
     */
    private boolean isInvalidContext(@Nullable Context context,
                                     @NonNull SensorListener listener,
                                     boolean isSubscribeAction) {
        if (context != null) return false;

        if (isSubscribeAction) listener.onSubscribe(UNREACHABLE_CONTEXT);
        else listener.onUnsubscribe(UNREACHABLE_CONTEXT);
        onUnreachableContext();
        return true;
    }

    private void callWithValidToken(@NonNull final AccessTokenListener listener, final String... permissionTypes) {
        final Context context = getContext();

        if (context == null) {
            listener.onFailure(new Exception(IKStatus.INPUT_KIT_UNREACHABLE_CONTEXT));
            return;
        }

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null && account.isExpired()) {
            startSilentLoggedIn(context, listener, permissionTypes);
            return;
        }

        if (account == null) {
            // If we don't have last signed in account then client should call authorize request first.
            listener.onFailure(new Exception(IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        Log.d(TAG, "=========== @@@TOKEN IS VALID@@@ ===========");
        listener.onSuccess();
    }

    /**
     * Perform Sign-in Interactively.
     * This is required if user never logged-in with Google Account before.
     * https://developers.google.com/games/services/android/signin#performing_interactive_sign-in
     *
     * @param permissionTypes Sample data type of permission that we need to ask for.
     */
    private void startSignedInAndAskForPermission(String... permissionTypes) {
        // Performing UI logged in only if possible.
        if (getHostActivity() != null && getContext() != null) {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(getContext(), getOptions(permissionTypes));
            Intent intent = signInClient.getSignInIntent();
            getHostActivity().startActivityForResult(intent, GF_PERMISSION_REQUEST_CODE);
        }
    }

    /**
     * Start silent logged in to Access Fit API in case short-lived access token invalid.
     * @param context           Current application context
     * @param listener          Access token listener
     * @param permissionTypes   Sample data type of permission that we need to ask for.
     */
    private void startSilentLoggedIn(@NonNull Context context,
                                     @NonNull final AccessTokenListener listener,
                                     final String... permissionTypes) {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        if (account != null) {
            Log.d(TAG, "=========== !!!FITNESS ACCESS TOKEN IS INVALID!!! ===========");
            Log.d(TAG, "=========== !!!STARTING TO PERFORM SILENT LOGIN!!! ===========");
            GoogleSignIn.getClient(context, getOptions(permissionTypes))
                    .silentSignIn()
                    .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                        @Override
                        public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "=========== !!!RENEWAL ACCESS TOKEN SUCCESS!!! ===========");
                                listener.onSuccess();
                            } else {
                                Log.d(TAG, "=========== !!!RENEWAL ACCESS TOKEN FAILED!!! ===========");
                                Exception err = new Exception("Unable to perform silent logged in!");
                                if (task.getException() != null) {
                                    err = task.getException();
                                }
                                err.printStackTrace();
                                listener.onFailure(err);

                                // FIXME:
                                // If we are unable to perform silent logged in, then we have no choice
                                // unless we perform interactive logged in.
                                // BUT it also has a drawback, due to popup might appear a couple times
                                // each time silent logged in fail.
                                // startSignedInAndAskForPermission(permissionTypes);
                            }
                        }
                    });
        }
    }

    /**
     * Get default google sign in options that being used for Google Fit.
     * @param permissionTypes Sample data type of permission that we need to ask for.
     * @return {@link GoogleSignInOptions}
     */
    private GoogleSignInOptions getOptions(String... permissionTypes) {
        return new GoogleSignInOptions.Builder()
                .requestId()
                .requestEmail()
                .addExtension(FitPermissionSet.getInstance().getPermissionsSet(permissionTypes))
                .build();
    }

    interface AccessTokenListener {
        void onSuccess();
        void onFailure(Exception e);
    }
}
