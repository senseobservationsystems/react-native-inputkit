package nl.sense.rninputkit.modules;


import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import nl.sense.rninputkit.data.Constants;
import nl.sense.rninputkit.data.ProviderName;
import nl.sense.rninputkit.helper.ValueConverter;
import nl.sense.rninputkit.helper.WeightConverter;
import nl.sense.rninputkit.modules.health.HealthPermissionPromise;
import nl.sense.rninputkit.modules.health.event.EventHandler;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.HealthProvider; // TODO IMPORTS
import nl.sense.rninputkit.inputkit.HealthProvider.ProviderType;
import nl.sense.rninputkit.inputkit.InputKit;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.Weight;
import nl.sense.rninputkit.inputkit.googlefit.GoogleFitHealthProvider;
import nl.sense.rninputkit.inputkit.helper.AppHelper;
import nl.sense.rninputkit.inputkit.status.IKProviderInfo;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

import static nl.sense.rninputkit.data.Constants.JS_SUPPORTED_EVENTS;
import static nl.sense.rninputkit.inputkit.constant.IKStatus.Code.IK_NOT_CONNECTED;

/**
 * Created by panjiyudasetya on 5/30/17.
 */

public class HealthBridge extends ReactContextBaseJavaModule implements ActivityEventListener,
        LifecycleEventListener {

    private static final String HEALTH_FIT_MODULE_NAME = "HealthBridge";
    private static final String TAG = HEALTH_FIT_MODULE_NAME;
    private ReactApplicationContext mReactContext;
    private InputKit mInputKit;
    private List<HealthPermissionPromise> mRequestHealthPromises;
    private ProviderType mActiveProvider;

    @SuppressWarnings("unused") // Used by React Native
    public HealthBridge(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        mReactContext.addLifecycleEventListener(this);
        mReactContext.addActivityEventListener(this);


        mRequestHealthPromises = new ArrayList<>();
        mActiveProvider = ProviderType.GOOGLE_FIT;
    }

    @Override
    public String getName() {
        return HEALTH_FIT_MODULE_NAME;
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Request Code : " + requestCode);
        if (requestCode == GoogleFitHealthProvider.GF_PERMISSION_REQUEST_CODE) {
            handlePromises(resultCode == Activity.RESULT_OK);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // At this point, we don't need to implement anything here
        // since we only wants to maintain activity results from
        // ConnectionResult#startResolutionForResult()
    }

    /**
     * Start monitoring health sensors.
     * @param typeString    Sensor type should be one of these {@link nl.sense.rninputkit.inputkit.constant.SampleType.SampleName} sensor
     * @param promise contains an information of subscribing health sensor.
     */
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native Application
    public void startMonitoring(final String typeString, final Promise promise) {
        switch (mActiveProvider) {
            case GOOGLE_FIT:
                promise.resolve(null);
                break;

            default:
                String notSupportedMsg = "Monitoring " + typeString + " is not supported.";
                promise.reject(String.valueOf(IKStatus.Code.INVALID_REQUEST), notSupportedMsg);
                break;
        }
    }

    /**
     * Stop monitoring health sensors.
     * @param typeString    Sensor type should be one of these {@link nl.sense.rninputkit.inputkit.constant.SampleType.SampleName} sensor
     * @param promise contains an information of unsubscribing health sensor.
     */
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native Application
    public void stopMonitoring(final String typeString, final Promise promise) {
        switch (mActiveProvider) {
            case GOOGLE_FIT:
                promise.resolve(null);
                break;

            default:
                String notSupportedMsg = "Monitoring " + typeString + " is not supported.";
                promise.reject(String.valueOf(IKStatus.Code.INVALID_REQUEST), notSupportedMsg);
                break;
        }
    }

    /**
     * Check Input Kit availability.
     * @param promise contains an information whether successfully connect to Input Kit or not.
     */
    @Deprecated
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void isAvailable(final Promise promise) {
        if (!mInputKit.isAvailable()) {
            Log.d(TAG, "isAvailable: Make sure to call request permission before called this function");
            promise.reject(String.valueOf(IK_NOT_CONNECTED), IKStatus.INPUT_KIT_NOT_CONNECTED);
            return;
        }

        if (mInputKit.isAvailable()) promise.resolve(true);
    }

    /**
     * Check Health Provider installation.
     * @param promise contains an information either health provider installed or not.
     */
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void isProviderInstalled(String providerName, Promise promise) {
        // Make sure provider name is not null
        if (TextUtils.isEmpty(providerName)) {
            promise.reject(
                String.valueOf(IKStatus.Code.INVALID_REQUEST),
                "Provider name must be provided!"
            );
            return;
        }

        // TODO : Add another handler for supported health providers
        if (providerName.equals(ProviderName.GOOGLE_FIT)) {
            if (AppHelper.isGoogleFitInstalled(mReactContext)) {
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
            return;
        }

        promise.reject(
            String.valueOf(IKStatus.Code.INVALID_REQUEST),
            providerName + " is not supported in InputKit!"
        );
    }

    /**
     * Check whether permission has been authorised or not.
     * @param permissions permission that needs to be checked
     * @param promise resolved whenever permission has been authorised
     *                rejected if it hasn;t
     */
    @Deprecated
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void isPermissionsAuthorised(ReadableArray permissions, final Promise promise) {
        if (!mInputKit.isPermissionsAuthorised(getConvertedPermission(permissions))) {
            Log.d(TAG, "isAvailable: Make sure to call request permission before called this function");
            promise.reject(String.valueOf(IK_NOT_CONNECTED), IKStatus.INPUT_KIT_NOT_CONNECTED);
            return;
        }

        promise.resolve(true);
    }

    /**
     * Request all related permission for specific API.
     * @param permissions containing an array of api permission.<br/>
     *                    For example : ["sleep", "stepCount"]
     * @param promise contains an information whether permissions successfully granted or not.
     */
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void requestPermissions(ReadableArray permissions, final Promise promise) {
        mInputKit.authorize(new InputKit.Callback() {
            @Override
            public void onAvailable(String... addMessages) {
                promise.resolve("CONNECTED_TO_INPUT_KIT");
            }

            @Override
            public void onNotAvailable(@NonNull IKResultInfo reason) {
                promise.reject(String.valueOf(reason.getResultCode()), reason.getMessage());
            }

            @Override
            public void onConnectionRefused(@NonNull IKProviderInfo providerInfo) {
                String message = providerInfo.getMessage();
                if (message.equals(IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS)) {
                    mRequestHealthPromises.add(new HealthPermissionPromise(promise, providerInfo));
                } else {
                    promise.reject(String.valueOf(
                            providerInfo.getResultCode()),
                            message);
                }
            }
        }, getConvertedPermission(permissions));
    }


    /**
     * Get total steps count of specific range
     * @param startTime epoch for the start date
     * @param endTime   epoch for the end date
     * @param promise containing number of total steps count
     */
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void getStepCount(final Double startTime,
                             final Double endTime,
                             final Promise promise) {
        Log.d(TAG, "getStepCount: " + startTime + ", " + endTime);
        mInputKit.getStepCount(
                startTime.longValue(),
                endTime.longValue(),
                0,
                new InputKit.Result<Integer>() {
                    @Override
                    public void onNewData(Integer data) {
                        Log.d(TAG, "getStepCount#onNewData: " + data);
                        promise.resolve(data);
                    }

                    @Override
                    public void onError(@NonNull IKResultInfo error) {
                        promise.reject(String.valueOf(error.getResultCode()), error.getMessage());
                    }
                });
    }

    /**
     *  Returns Promise contains distribution of step count value through out a specific range.
     *
     *  @param startTime    epoch for the start date of the range where the distribution should be calculated from.
     *  @param endTime      epoch for the end date of the range where the distribution should be calculated from.
     *  @param interval     Interval
     *  @param promise      containing:
     *     value: array of data points
     *     startDate: start date
     *     endDate: end date
     **/
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void getStepCountDistribution(final Double startTime,
                                         final Double endTime,
                                         final String interval,
                                         final Promise promise) {
        Log.d(TAG, "getStepCountDistribution: " + startTime + ", " + endTime + ", " + interval);
        mInputKit.getStepCountDistribution(
                startTime.longValue(),
                endTime.longValue(),
                interval,
                0,
                new InputKit.Result<StepContent>() {
                    @Override
                    public void onNewData(StepContent data) {
                        Log.d(TAG, "getStepCountDistribution#onNewData: " + data.toJson());
                        WritableMap object = ValueConverter.toWritableMap(data);
                        Log.d(TAG, "getStepCountDistribution#onNewData: CONVERTED " + object);
                        promise.resolve(object);
                    }

                    @Override
                    public void onError(@NonNull IKResultInfo error) {
                        promise.reject(String.valueOf(error.getResultCode()), error.getMessage());
                    }
                });
    }

    /**
     * Start tracking specific sensor.
     *
     * @param sampleType Sample type should be one of these {@link SampleType.SampleName} sensor
     * @param startTime  Start time of sensor tracking. Actually on Android is not necessary since
     *                   it will use refresh rate
     * @param promise    Containing an information of request code and code message whether
     *                   tracking action successfully or not
     */
    @Deprecated
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void startTracking(final String sampleType,
                              final Double startTime,
                              final Promise promise) {
        mInputKit.startTracking(
                sampleType,
                Pair.create(1, TimeUnit.MINUTES),
                new HealthProvider.SensorListener<SensorDataPoint>() {
                    @Override
                    public void onSubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            promise.resolve(info.getMessage());
                            return;
                        }
                        promise.reject(String.valueOf(info.getResultCode()), info.getMessage());
                    }

                    @Override
                    public void onReceive(@NonNull SensorDataPoint data) {
                        if (mReactContext != null) {
                            EventHandler.emit(
                                    mReactContext.getApplicationContext(),
                                    JS_SUPPORTED_EVENTS.get(Constants.EVENTS.inputKitTracking),
                                    data,
                                    // TODO : Does completion callback is necessary?
                                    new Callback() {
                                        @Override
                                        public void invoke(Object... args) {

                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onUnsubscribe(@NonNull IKResultInfo info) {
                        promise.reject(String.valueOf(info.getResultCode()), info.getMessage());
                    }
                });
    }

    /**
     * Stop tracking specific sensor.
     *
     * @param sampleType Sample type should be one of these {@link SampleType.SampleName} sensor
     * @param promise    Containing an information of request code and code message whether
     *                   tracking action successfully or not
     */
    @Deprecated
    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void stopTracking(final String sampleType,
                             final Promise promise) {
        mInputKit.stopTracking(
                sampleType,
                new HealthProvider.SensorListener<SensorDataPoint>() {
                    @Override
                    public void onSubscribe(@NonNull IKResultInfo info) { }

                    @Override
                    public void onReceive(@NonNull SensorDataPoint data) { }

                    @Override
                    public void onUnsubscribe(@NonNull IKResultInfo info) {
                        if (info.getResultCode() == IKStatus.Code.VALID_REQUEST) {
                            promise.resolve(info.getMessage());
                            return;
                        }
                        promise.reject(String.valueOf(info.getResultCode()), info.getMessage());
                    }
                });
    }
    @Override
    public void onHostResume() {
        // Do nothing here, as long as host module didn't destroyed,
        // we still able to obtain sensor manager & subscribe listener
        if (mInputKit == null) {
            mInputKit = InputKit.getInstance(mReactContext);
            mInputKit.setHealthProvider(mActiveProvider);
        }
        mInputKit.setHostActivity(getCurrentActivity());
    }

    @Override
    public void onHostPause() {
        // Do nothing here, as long as host module didn't destroyed,
        // we still able to obtain sensor manager & subscribe listener
    }

    @Override
    public void onHostDestroy() {
        // Do nothing here, as long as host module didn't destroyed,
        // we still able to obtain sensor manager & subscribe listener
    }

    private void handlePromises(boolean isResolved) {
        String message = "CONNECTED_TO_INPUT_KIT";
        // Resolve promises, last in first out
        for (int i = mRequestHealthPromises.size(); i > 0; i--) {
            HealthPermissionPromise permissionPromise = mRequestHealthPromises.get(i - 1);
            if (isResolved) {
                permissionPromise
                        .getPromise()
                        .resolve(message);
            } else {
                IKProviderInfo info = permissionPromise.getProviderInfo();
                permissionPromise
                        .getPromise()
                        .reject(String.valueOf(info.getResultCode()), info.getMessage());
            }
        }
        mRequestHealthPromises.clear();
    }

    private String[] getConvertedPermission(ReadableArray permissionTypes) {
        if (permissionTypes == null || permissionTypes.size() == 0) {
            return new String[0];
        }

        List<String> converted = new ArrayList<>();
        for (Object permissionType : permissionTypes.toArrayList()) {
            if (String.valueOf(permissionType).equals(SampleType.STEP_COUNT)
                    || String.valueOf(permissionType).equals(SampleType.DISTANCE_WALKING_RUNNING)
                    || String.valueOf(permissionType).equals(SampleType.WEIGHT)) {
                converted.add(String.valueOf(permissionType));
            }
        }
        return converted.toArray(new String[]{});
    }
}
