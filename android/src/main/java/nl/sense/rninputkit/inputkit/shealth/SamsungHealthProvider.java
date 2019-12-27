package nl.sense.rninputkit.inputkit.shealth;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.HealthProvider;
import nl.sense.rninputkit.inputkit.HealthProvider;
import nl.sense.rninputkit.inputkit.InputKit;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.entity.BloodPressure;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.entity.Weight;
import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.helper.InputKitTimeUtils;
import nl.sense.rninputkit.inputkit.shealth.utils.SHealthUtils;
import nl.sense.rninputkit.inputkit.status.IKProviderInfo;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by xedi on 10/18/17.
 */

public class SamsungHealthProvider extends HealthProvider {
    private static final IKResultInfo REQUIRED_PERMISSION = new IKResultInfo(
            IKStatus.Code.S_HEALTH_PERMISSION_REQUIRED,
            IKStatus.INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS
    );
    private static final IKResultInfo DISCONNECTED = new IKResultInfo(
            IKStatus.Code.S_HEALTH_DISCONNECTED,
            IKStatus.INPUT_KIT_DISCONNECTED);
    private static final IKResultInfo UNSUPPORTED_REALTIME_TRACKING = new IKResultInfo(
            IKStatus.Code.INVALID_REQUEST,
            "Sample type is not supported for real time monitoring");

    private SHealthWrapper mSHealthWrapper;

    public SamsungHealthProvider(@NonNull Context context) {
        super(context);
        mSHealthWrapper = new SHealthWrapper(context);
    }

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Nullable
    @Override
    public Activity getHostActivity() {
        return super.getHostActivity();
    }

    @Override
    public void setHostActivity(@Nullable Activity activity) {
        super.setHostActivity(activity);
    }

    @Override
    protected boolean isAvailable(@NonNull final InputKit.Result callback) {
        return super.isAvailable(callback);
    }

    @Override
    public boolean isAvailable() {
        return (mSHealthWrapper.getConnectionStatus() == SHealthConstant.STATUS_CONNECTED);
    }

    @Override
    public boolean isPermissionsAuthorised(String[] permissionTypes) {
        if (permissionTypes == null || permissionTypes.length == 0) {
            return false;
        }

        Set<HealthPermissionManager.PermissionKey> permissionSet =
                mSHealthWrapper.generatePermissionKeySet(permissionTypes);
        return mSHealthWrapper.isPermissionAcquired(permissionSet);
    }

    @Override
    public void authorize(@NonNull final InputKit.Callback callback, final String... permissionType) {
        mSHealthWrapper.connectService(new SHealthWrapper.OnConnectCallback() {
            @Override
            public void onResult(int statusCode, HealthConnectionErrorResult errorInfo) {
                if (statusCode == SHealthConstant.STATUS_CONNECTED) {
                    checkPermissions(callback, permissionType);
                } else if (statusCode == SHealthConstant.STATUS_DISCONNECTED) {
                    callback.onNotAvailable(DISCONNECTED);
                } else if (statusCode == SHealthConstant.STATUS_ERROR) {
                    int errorCode = IKStatus.Code.S_HEALTH_CONNECTION_ERROR;
                    String msg = IKStatus.INPUT_KIT_CONNECTION_ERROR;
                    if (errorInfo != null) {
                        errorCode = errorInfo.getErrorCode();
                        msg = getErrorMessage(errorCode);
                    }
                    callback.onConnectionRefused(new IKProviderInfo(errorCode, msg));
                }
            }
        });
    }

    @Override
    public void disconnect(@NonNull InputKit.Result<Boolean> callback) {
        mSHealthWrapper.disconnect();
    }

    @Override
    public void getDistance(long startTime, long endTime, int limit, @NonNull InputKit.Result<Float> callback) {
        mSHealthWrapper.getStepDistance(adjustTimeToUTC(startTime), adjustTimeToUTC(endTime), callback);
    }

    @Override
    public void getDistanceSamples(long startTime, long endTime, int limit,
                                   @NonNull InputKit.Result<List<IKValue<Float>>> callback) {
        mSHealthWrapper.getStepDistanceSamples(adjustTimeToUTC(startTime), adjustTimeToUTC(endTime), limit, callback);
    }

    @Override
    public void getStepCount(@NonNull InputKit.Result<Integer> callback) {
        long startTime = InputKitTimeUtils.getTodayStartTime();
        long endTime = startTime + InputKitTimeUtils.ONE_DAY;
        mSHealthWrapper.getStepCount(startTime, endTime, callback);
    }

    @Override
    public void getStepCount(long startTime, long endTime, int limit,
                             @NonNull InputKit.Result<Integer> callback) {
        mSHealthWrapper.getStepCount(adjustTimeToUTC(startTime), adjustTimeToUTC(endTime), callback);
    }

    @Override
    public void getStepCountDistribution(long startTime, long endTime,
                                         @NonNull @Interval.IntervalName String interval,
                                         int limit, @NonNull InputKit.Result<StepContent> callback) {
        TimeInterval timeInterval = new TimeInterval(interval);
        Options options = new Options.Builder()
                .startTime(adjustTimeToUTC(startTime))
                .endTime(adjustTimeToUTC(endTime))
                .timeInterval(timeInterval)
                .useDataAggregation()
                .build();
        mSHealthWrapper.getStepCountDistribution(options, limit, callback);
    }

    @Override
    public void getSleepAnalysisSamples(long startTime, long endTime,
                                        @NonNull InputKit.Result<List<IKValue<String>>> callback) {
        mSHealthWrapper.getSleepAnalysisSamples(adjustTimeToUTC(startTime), adjustTimeToUTC(endTime), callback);
    }

    @Override
    public void getBloodPressure(long startTime, long endTime,
                                 @NonNull InputKit.Result<List<BloodPressure>> callback) {
        mSHealthWrapper.getBloodPressure(adjustTimeToUTC(startTime), adjustTimeToUTC(endTime), callback);
    }

    @Override
    public void getWeight(long startTime, long endTime,
                          @NonNull InputKit.Result<List<Weight>> callback) {
        mSHealthWrapper.getWeight(adjustTimeToUTC(startTime), adjustTimeToUTC(endTime), callback);
    }

    public void getListDataType(@NonNull InputKit.Result<List<String>> callback) {
        mSHealthWrapper.getListDataType(callback);
    }

    @Override
    public void startMonitoring(@NonNull @SampleType.SampleName String sensorType,
                                @NonNull Pair<Integer, TimeUnit> samplingRate,
                                @NonNull SensorListener<SensorDataPoint> listener) {
        if (sensorType.equals(SampleType.STEP_COUNT)) {
            long startTime = System.currentTimeMillis();
            mSHealthWrapper.monitorStep(sensorType, adjustTimeToUTC(startTime), listener);
        } else listener.onSubscribe(UNSUPPORTED_REALTIME_TRACKING);
    }

    @Override
    public void stopMonitoring(@NonNull @SampleType.SampleName String sensorType,
                               @NonNull SensorListener<SensorDataPoint> listener) {
        if (sensorType.equals(SampleType.STEP_COUNT)) {
            mSHealthWrapper.stopMonitorStep(sensorType, listener);
        } else listener.onSubscribe(UNSUPPORTED_REALTIME_TRACKING);
    }

    @Override
    public void startTracking(@NonNull @SampleType.SampleName String sensorType,
                              @NonNull Pair<Integer, TimeUnit> samplingRate,
                              @NonNull SensorListener<SensorDataPoint> listener) {
        if (sensorType.equals(SampleType.STEP_COUNT)) {
            long startTime = System.currentTimeMillis();
            mSHealthWrapper.monitorStep(sensorType, adjustTimeToUTC(startTime), listener);
        } else listener.onSubscribe(UNSUPPORTED_REALTIME_TRACKING);
    }

    @Override
    public void stopTracking(@NonNull String sensorType,
                             @NonNull SensorListener<SensorDataPoint> listener) {
        if (sensorType.equals(SampleType.STEP_COUNT)) {
            mSHealthWrapper.stopMonitorStep(sensorType, listener);
        } else listener.onSubscribe(UNSUPPORTED_REALTIME_TRACKING);
    }

    @Override
    public void stopTrackingAll(@NonNull SensorListener<SensorDataPoint> listener) {
        mSHealthWrapper.stopMonitorStep("", listener);
    }

    private void checkPermissions(@NonNull final InputKit.Callback callback, String... permissionType) {
        mSHealthWrapper.authorize(new SHealthWrapper.OnPermissionCallback() {
            @Override
            public void onResult(int resultCode) {
                if (resultCode == SHealthConstant.STATUS_CONNECTED) {
                    callback.onAvailable();
                } else if (resultCode == SHealthConstant.STATUS_DISCONNECTED) {
                    callback.onNotAvailable(REQUIRED_PERMISSION);
                }
            }
        }, false, permissionType);
    }

    private String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                return IKStatus.SAMSUNG_HEALTH_NOT_INSTALLED;
            case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                return IKStatus.SAMSUNG_HEALTH_OLD_VERSION;
            case HealthConnectionErrorResult.PLATFORM_DISABLED:
                return  IKStatus.SAMSUNG_HEALTH_DISABLED;
            case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                return IKStatus.SAMSUNG_HEALTH_USER_AGREEMENT_NEEDED;
            default: return IKStatus.SAMSUNG_HEALTH_IS_NOT_AVAILABLE;
        }
    }

    private long adjustTimeToUTC(long time) {
        return time + SHealthUtils.timeDiff();
    }
}
