package nl.sense.rninputkit.inputkit.shealth;

import android.os.Handler;
import android.os.Looper;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.HealthProvider;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.shealth.utils.DataMapper;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by xedi on 11/7/17.
 */

public class StepMonitor {
    private static final String ALIAS_TOTAL_COUNT = "count";
    private static final String ALIAS_TOTAL_DISTANCE = "distance";
    private static final String ALIAS_DEVICE_UUID = "deviceuuid";

    private static final int PERIOD_IN_MS = 15 * 1000;
    private long mStartTime;
    private HealthProvider.SensorListener<SensorDataPoint> mRealTimeStepListener;
    private Handler mHandler = null;
    private HealthDataResolver mResolver = null;
    private String mSensorType;
    private boolean isStopped = false;
    private Runnable mTaskRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isStopped) {
                executeMonitoring();
                mHandler.postDelayed(this, TimeUnit.MILLISECONDS.toMillis(PERIOD_IN_MS));
            }
        }
    };
    private HealthResultHolder.ResultListener<HealthDataResolver.AggregateResult> mStepListener =
            new HealthResultHolder.ResultListener<HealthDataResolver.AggregateResult>() {
                @Override
                public void onResult(HealthDataResolver.AggregateResult healthDatas) {
                    String deviceUuid = null;
                    int totalCount = 0;
                    float totalDistance = 0.0f;
                    try {
                        Iterator<HealthData> iterator = healthDatas.iterator();
                        if (iterator.hasNext()) {
                            HealthData data = iterator.next();
                            deviceUuid = data.getString(ALIAS_DEVICE_UUID);
                            totalCount = data.getInt(ALIAS_TOTAL_COUNT);
                            totalDistance = data.getFloat(ALIAS_TOTAL_DISTANCE);
                        }
                    } finally {
                        healthDatas.close();
                    }
                    if (mRealTimeStepListener != null) {
                        mRealTimeStepListener.onReceive(
                                DataMapper.toSensorDataPoint(mSensorType,
                                        mStartTime, totalCount,
                                        totalDistance));
                        monitorStepDataTask();
                    }
                }
            };

    StepMonitor(long startTime, String sensorType, HealthDataResolver resolver,
                HealthProvider.SensorListener<SensorDataPoint> listener) {
        mStartTime = startTime;
        mResolver = resolver;
        mRealTimeStepListener = listener;
        mSensorType = sensorType;
        executeMonitoring();
    }

    private void executeMonitoring() {
        long stopTime = mStartTime + SHealthConstant.ONE_DAY;
        HealthDataResolver.AggregateRequest request = aggregateStep(mStartTime, stopTime);
        try {
            mResolver.aggregate(request).setResultListener(mStepListener);
        } catch (Exception e) {
            mRealTimeStepListener.onUnsubscribe(
                    new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR, e.getMessage()));
            stopMonitor(false);
        }
    }

    public void stopMonitor(boolean callCallback) {
        if (mHandler != null) {
            mHandler.removeCallbacks(mTaskRunnable);
            if (callCallback && !isStopped) {
                mRealTimeStepListener.onUnsubscribe(
                        new IKResultInfo(IKStatus.Code.VALID_REQUEST,
                                IKStatus.INPUT_KIT_MONITOR_UNREGISTERED));
            }
        }
        isStopped = true;
    }

    public void stopMonitor() {
        stopMonitor(true);
    }

    private void monitorStepDataTask() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(mTaskRunnable, TimeUnit.MILLISECONDS.toMillis(PERIOD_IN_MS));
        }
    }

    private HealthDataResolver.AggregateRequest aggregateStep(long startTime, long stopTime) {
        return new HealthDataResolver.AggregateRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .addFunction(HealthDataResolver.AggregateRequest.AggregateFunction.SUM,
                        HealthConstants.StepCount.COUNT, ALIAS_TOTAL_COUNT)
                .addFunction(HealthDataResolver.AggregateRequest.AggregateFunction.SUM,
                        HealthConstants.StepCount.DISTANCE, ALIAS_TOTAL_DISTANCE)
                .addGroup(HealthConstants.StepCount.DEVICE_UUID, ALIAS_DEVICE_UUID)
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME,
                        HealthConstants.StepCount.TIME_OFFSET,
                        startTime, stopTime)
                .setSort(ALIAS_TOTAL_COUNT, HealthDataResolver.SortOrder.DESC)
                .build();
    }
}
