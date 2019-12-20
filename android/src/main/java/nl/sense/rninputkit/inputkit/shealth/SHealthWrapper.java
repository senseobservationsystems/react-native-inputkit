package nl.sense.rninputkit.inputkit.shealth;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.sense.rninputkit.inputkit.HealthProvider;
import nl.sense.rninputkit.inputkit.InputKit;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.entity.BloodPressure;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.Weight;
import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.shealth.utils.SHealthPermissionSet;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;


/**
 * Created by xedi on 9/20/17.
 */

public class SHealthWrapper {
    public static final String TAG = "SHealthWrapper";
    private HealthDataStore mStore;
    private StepCountReader mStepReader;
    private SleepReader mSleepReader;
    private BloodPressureReader mBloodPressureReader;
    private WeightReader mWeightReader;
    private SHealthPermissionSet mPermissionSet;

    private boolean mFinished = true;
    private int mConnectionStatus = SHealthConstant.STATUS_DISCONNECTED;
    private HealthConnectionErrorResult mLastConnectionError = null;
    private OnConnectCallback mCurrentConnectCallback = null;
    private final HealthDataStore.ConnectionListener mConnectionListener =
            new HealthDataStore.ConnectionListener() {
                @Override
                public void onConnected() {
                    mConnectionStatus = SHealthConstant.STATUS_CONNECTED;
                    mLastConnectionError = null;
                    if (mCurrentConnectCallback != null) {
                        mCurrentConnectCallback.onResult(mConnectionStatus, null);
                    }
                    Log.d(TAG, "onConnected");
                }

                @Override
                public void onConnectionFailed(HealthConnectionErrorResult error) {
                    mConnectionStatus = SHealthConstant.STATUS_ERROR;
                    mLastConnectionError = error;
                    if (mCurrentConnectCallback != null) {
                        mCurrentConnectCallback.onResult(mConnectionStatus, mLastConnectionError);
                    }
                    Log.d(TAG, "onConnectionFailed");
                }

                @Override
                public void onDisconnected() {
                    mConnectionStatus = SHealthConstant.STATUS_DISCONNECTED;
                    mLastConnectionError = null;
                    if (mCurrentConnectCallback != null) {
                        mCurrentConnectCallback.onResult(mConnectionStatus, null);
                    }
                    Log.d(TAG, "onDisconnected");
                    if (!isFinishing()) {
                        mStore.connectService();
                    }
                }
            };
    private OnPermissionCallback mCurrentPermissionCallback = null;
    private final HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult> mPermissionListener =
            new HealthResultHolder.ResultListener<HealthPermissionManager.PermissionResult>() {
                @Override
                public void onResult(HealthPermissionManager.PermissionResult result) {
                    Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = result.getResultMap();
                    if (resultMap.values().contains(Boolean.FALSE)) {
                        mCurrentPermissionCallback.onResult(SHealthConstant.PERMISSION_DENIED);
                    } else {
                        mCurrentPermissionCallback.onResult(SHealthConstant.PERMISSION_GRANTED);
                    }
                }
            };

    public SHealthWrapper(Context context) {
        mFinished = false;
        mPermissionSet = SHealthPermissionSet.getInstance();
        initialize(context);
        initReporter();
    }

    private void initialize(Context context) {
        HealthDataService healthDataService = new HealthDataService();
        try {
            healthDataService.initialize(context);
        } catch (Exception e) {
            e.printStackTrace();
            mConnectionStatus = SHealthConstant.STATUS_ERROR_INIT;
        }
        mStore = new HealthDataStore(context, mConnectionListener);
        mStore.connectService();
    }

    public void connectService(OnConnectCallback connectCallback) {
        mCurrentConnectCallback = connectCallback;
        if (mConnectionStatus == SHealthConstant.STATUS_CONNECTED) {
            connectCallback.onResult(mConnectionStatus, mLastConnectionError);
        } else {
            mStore.connectService();
        }
    }

    public void disconnect() {
        mConnectionStatus = SHealthConstant.STATUS_DISCONNECTED;
        mLastConnectionError = null;
        mStore.disconnectService();
    }

    public boolean isFinishing() {
        return mFinished;
    }

    public HealthConnectionErrorResult getLastConnectionError() {
        return mLastConnectionError;
    }

    public int getConnectionStatus() {
        return mConnectionStatus;
    }

    public void authorize(OnPermissionCallback permissionCallback,
                          boolean forceShowPermission,
                          String... permissionType) {
        mCurrentPermissionCallback = permissionCallback;

        if (forceShowPermission || !isPermissionAcquired(generatePermissionKeySet(permissionType))) {
            requestPermission(permissionType);
            return;
        }

        mCurrentPermissionCallback.onResult(SHealthConstant.PERMISSION_GRANTED);
    }

    public void authorize(OnPermissionCallback permissionCallback) {
        authorize(permissionCallback, false);
    }

    public void getStepCount(long startTime, long endTime,
                             @NonNull InputKit.Result<Integer> callback) {
        mStepReader.readStepCount(startTime, endTime, callback);
    }

    public void getStepCountDistribution(Options options, int limit,
                                         InputKit.Result<StepContent> callback) {
        mStepReader.readStepCountHistories(options, limit, callback);
    }

    public void monitorStep(String sensorType, long startTime,
                            @NonNull HealthProvider.SensorListener<SensorDataPoint> listener) {
        if (!isPermissionAcquired(mPermissionSet.getStepPermissionSet())) {
            listener.onUnsubscribe(new IKResultInfo(IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        mStepReader.monitorStepData(sensorType, startTime, listener);
    }

    public void stopMonitorStep(String sensorType,
                                @NonNull HealthProvider.SensorListener<SensorDataPoint> listener) {
        mStepReader.stopMonitorStepData(sensorType, listener);
    }

    public void getStepDistance(long startTime, long endTime,
                                InputKit.Result<Float> callback) {
        if (!isPermissionAcquired(mPermissionSet.getStepPermissionSet())) {
            callback.onError(new IKResultInfo(IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        mStepReader.readStepDistance(startTime, endTime, callback);
    }

    public void getStepDistanceSamples(long startTime, long endTime, int limit,
                                       @NonNull InputKit.Result<List<IKValue<Float>>> callback) {
        if (!isPermissionAcquired(mPermissionSet.getStepPermissionSet())) {
            callback.onError(new IKResultInfo(IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        mStepReader.readStepDistanceSamples(startTime, endTime, limit, callback);
    }

    public void getSleepAnalysisSamples(long startTime, long endTime,
                                        @NonNull InputKit.Result<List<IKValue<String>>> callback) {
        if (!isPermissionAcquired(mPermissionSet.getSleepPermissionSet())) {
            callback.onError(new IKResultInfo(IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        mSleepReader.readSleep(startTime, endTime, callback);
    }

    public void getBloodPressure(long startTime, long endTime,
                                 @NonNull InputKit.Result<List<BloodPressure>> callback) {
        if (!isPermissionAcquired(mPermissionSet.getBloodPressurePermissionSet())) {
            callback.onError(new IKResultInfo(IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        mBloodPressureReader.readBloodPressure(startTime, endTime, callback);
    }

    public void getWeight(long startTime, long endTime,
                          @NonNull InputKit.Result<List<Weight>> callback) {
        if (!isPermissionAcquired(mPermissionSet.getWeightPermissionSet())) {
            callback.onError(new IKResultInfo(IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
                    IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS));
            return;
        }

        mWeightReader.readWeight(startTime, endTime, callback);
    }

    public void getListDataType(@NonNull InputKit.Result<List<String>> callback) {
        callback.onNewData(SHealthConstant.SUPPORTED_DATA_TYPES);
    }

    private void initReporter() {
        mStepReader = new StepCountReader(mStore);
        mSleepReader = new SleepReader(mStore);
        mBloodPressureReader = new BloodPressureReader(mStore);
        mWeightReader = new WeightReader(mStore);
    }

    public boolean isPermissionAcquired(Set<HealthPermissionManager.PermissionKey> sets) {
        if (sets == null || sets.size() == 0) {
            return false;
        }

        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            Map<HealthPermissionManager.PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(sets);
            return !resultMap.values().contains(Boolean.FALSE);
        } catch (Exception e) {
            Log.e(TAG, "Permission request fails.", e);
        }
        return false;
    }

    private void requestPermission(String... permissionType) {
        HealthPermissionManager pmsManager = new HealthPermissionManager(mStore);
        try {
            pmsManager.requestPermissions(
                    generatePermissionKeySet(permissionType),
                    null
            ).setResultListener(mPermissionListener);
        } catch (Exception e) {
            Log.e(TAG, "Permission setting fails.", e);
            mCurrentPermissionCallback.onResult(SHealthConstant.PERMISSION_DENIED);
        }
    }

    public Set<HealthPermissionManager.PermissionKey> generatePermissionKeySet(String... permissionType) {
        if (permissionType == null || permissionType.length == 0) {
            return Collections.emptySet();
        }
        Set<HealthPermissionManager.PermissionKey> pmsKeySet = new HashSet<>();
        for (String permission : permissionType) {
            if (permission.equals(SampleType.STEP_COUNT) || permission.equals(SampleType.DISTANCE_WALKING_RUNNING)) {
                pmsKeySet.addAll(mPermissionSet.getStepPermissionSet());
            } else if (permission.equals(SampleType.SLEEP)) {
                pmsKeySet.addAll(mPermissionSet.getSleepPermissionSet());
            } else if (permission.equals(SampleType.WEIGHT)) {
                pmsKeySet.addAll(mPermissionSet.getWeightPermissionSet());
            } else if (permission.equals(SampleType.BLOOD_PRESSURE)) {
                pmsKeySet.addAll(mPermissionSet.getBloodPressurePermissionSet());
            }
        }
        return pmsKeySet;
    }

    public interface OnConnectCallback {
        void onResult(int statusCode, HealthConnectionErrorResult errorInfo);
    }

    public interface OnPermissionCallback {
        void onResult(int resultCode);
    }
}
