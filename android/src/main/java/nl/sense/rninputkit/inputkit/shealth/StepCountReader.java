/**
 * Copyright (C) Sense Health BV
 * modified from s-health sample
 */

package nl.sense.rninputkit.inputkit.shealth;


import androidx.annotation.NonNull;
import android.util.Pair;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest.AggregateFunction;
import com.samsung.android.sdk.healthdata.HealthDataResolver.AggregateRequest.TimeGroupUnit;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.SortOrder;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nl.sense.rninputkit.inputkit.HealthProvider;
import nl.sense.rninputkit.inputkit.InputKit;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.shealth.utils.DataMapper;
import nl.sense.rninputkit.inputkit.shealth.utils.SHealthUtils;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

public class StepCountReader {

    public static final String TAG = "S_HEALTH";
    public static final String STEP_SUMMARY_DATA_TYPE_NAME = "com.samsung.shealth.step_daily_trend";
    private static final String ALIAS_TOTAL_COUNT = "count";
    private static final String ALIAS_TOTAL_DISTANCE = "distance";
    private static final String ALIAS_DEVICE_UUID = "deviceuuid";
    private static final String ALIAS_BINNING_TIME = "binning_time";
    private final HealthDataResolver mResolver;
    private Map<String, StepMonitor> mStepMonitorMaps = new HashMap<>();

    public StepCountReader(HealthDataStore store) {
        mResolver = new HealthDataResolver(store, null);
    }

    public void readStepCount(final long startTime, final long stopTime,
                              @NonNull final InputKit.Result<Integer> callback) {
        requestStepData(startTime, stopTime, new StepRequestListener(callback) {
            @Override
            public void onStepCount(int count) {
                callback.onNewData(count);
            }
        });
    }

    public void readStepCountHistories(Options options, final int limit,
                                       @NonNull final InputKit.Result<StepContent> callback) {
        final long startTime = options.getStartTime();
        final long endTime = options.getEndTime();
        final TimeInterval timeInterval = options.getTimeInterval();
        requestStepData(startTime, endTime, new StepRequestListener(callback) {
            @Override
            void onDeviceId(String deviceId) {
                readStepCountHistories(startTime, endTime, limit, timeInterval, deviceId, callback);
            }
        });
    }

    public void readStepDistance(long startTime, long stopTime,
                                 @NonNull final InputKit.Result<Float> callback) {
        requestStepData(startTime, stopTime, new StepRequestListener(callback) {
            @Override
            void onStepDistance(float distance) {
                callback.onNewData(distance);
            }
        });
    }

    public void readStepDistanceSamples(final long startTime, final long stopTime, final int limit,
                                        @NonNull final InputKit.Result<List<IKValue<Float>>> callback) {
        requestStepData(startTime, stopTime, new StepRequestListener(callback) {
            @Override
            public void onDeviceId(String deviceId) {
                TimeInterval timeInterval = new TimeInterval(Interval.ONE_MINUTE);
                readStepDistanceHistories(startTime, stopTime, limit, timeInterval, deviceId, callback);
            }
        });
    }

    private void requestStepData(final long startTime, final long stopTime,
                                 @NonNull final StepRequestListener listener) {
        AggregateRequest request = aggregateStep(startTime, stopTime);
        try {
            mResolver.aggregate(request).setResultListener(
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
                                listener.onStepCount(totalCount);
                                listener.onStepDistance(totalDistance);
                                healthDatas.close();
                            }
                            if (deviceUuid != null) {
                                listener.onDeviceId(deviceUuid);
                            } else {
                                listener.onError(new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR,
                                        IKStatus.INPUT_KIT_NO_DEVICES_SOURCE));
                            }
                        }
                    });
        } catch (Exception e) {
            listener.onError(new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR, e.getMessage()));
        }
    }

    public void monitorStepData(String sensorType, long startTime,
                                HealthProvider.SensorListener<SensorDataPoint> realTimeStepListener) {
        if (mStepMonitorMaps.get(sensorType) == null) {
            StepMonitor sm = new StepMonitor(startTime,
                    sensorType,
                    mResolver,
                    realTimeStepListener);
            mStepMonitorMaps.put(sensorType, sm);
        } else {
            realTimeStepListener.onSubscribe(new IKResultInfo(IKStatus.Code.VALID_REQUEST,
                    IKStatus.INPUT_KIT_MONITOR_REGISTERED));
        }
    }

    public void stopMonitorStepData(String sensorType,
                                    HealthProvider.SensorListener<SensorDataPoint> realTimeStepListener) {
        if (sensorType.equals("")) {
            Iterator<Map.Entry<String, StepMonitor>> it = mStepMonitorMaps.entrySet().iterator();
            while (it.hasNext()) {
                StepMonitor sm = it.next().getValue();
                sm.stopMonitor();
            }
            mStepMonitorMaps.clear();
        } else {
            StepMonitor sm = mStepMonitorMaps.get(sensorType);
            if (sm == null) {
                realTimeStepListener.onSubscribe(new IKResultInfo(IKStatus.Code.VALID_REQUEST,
                        IKStatus.INPUT_KIT_MONITORING_NOT_AVAILABLE));
            } else {
                sm.stopMonitor();
                mStepMonitorMaps.remove(sensorType);
                realTimeStepListener.onSubscribe(new IKResultInfo(IKStatus.Code.VALID_REQUEST,
                        IKStatus.INPUT_KIT_MONITOR_REGISTERED));
            }
        }
    }

    private AggregateRequest aggregateStep(long startTime, long stopTime) {
        return new AggregateRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .addFunction(AggregateFunction.SUM, HealthConstants.StepCount.COUNT, ALIAS_TOTAL_COUNT)
                .addFunction(AggregateFunction.SUM, HealthConstants.StepCount.DISTANCE, ALIAS_TOTAL_DISTANCE)
                .addGroup(HealthConstants.StepCount.DEVICE_UUID, ALIAS_DEVICE_UUID)
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME, HealthConstants.StepCount.TIME_OFFSET,
                        startTime, stopTime)
                .setSort(ALIAS_TOTAL_COUNT, SortOrder.DESC)
                .build();
    }

    private AggregateRequest aggregateStepHistory(long startTime, long stopTime,
                                                  Pair<TimeGroupUnit, Integer> interval, String deviceUuid) {
        Filter filter = Filter.eq(HealthConstants.StepCount.DEVICE_UUID, deviceUuid);
        return new AggregateRequest.Builder()
                .setDataType(HealthConstants.StepCount.HEALTH_DATA_TYPE)
                .addFunction(AggregateFunction.SUM,
                        HealthConstants.StepCount.COUNT, ALIAS_TOTAL_COUNT)
                .addFunction(AggregateFunction.SUM,
                        HealthConstants.StepCount.DISTANCE, ALIAS_TOTAL_DISTANCE)
                .setTimeGroup(interval.first, interval.second,
                        HealthConstants.StepCount.START_TIME,
                        HealthConstants.StepCount.TIME_OFFSET,
                        ALIAS_BINNING_TIME)
                .setLocalTimeRange(HealthConstants.StepCount.START_TIME,
                        HealthConstants.StepCount.TIME_OFFSET, startTime, stopTime)
                .setFilter(filter)
                .setSort(ALIAS_BINNING_TIME, SortOrder.ASC)
                .build();
    }

    private void readStepCountHistories(final long startTime, final long stopTime, final int limit,
                                        TimeInterval timeInterval, String deviceUuid,
                                        @NonNull final InputKit.Result<StepContent> callbackStepHistory) {
        try {
            final Pair<TimeGroupUnit, Integer> ret = SHealthUtils.convertTimeInterval(timeInterval);
            AggregateRequest request = aggregateStepHistory(startTime, stopTime, ret, deviceUuid);
            mResolver.aggregate(request).setResultListener(
                    new HealthResultHolder.ResultListener<HealthDataResolver.AggregateResult>() {
                        @Override
                        public void onResult(HealthDataResolver.AggregateResult healthDatas) {
                            List<StepBinningData> binningCountArray = new ArrayList<>();
                            try {
                                for (HealthData data : healthDatas) {
                                    String binningTime = data.getString(ALIAS_BINNING_TIME);
                                    int binningCount = data.getInt(ALIAS_TOTAL_COUNT);
                                    float binningDistance = data.getInt(ALIAS_TOTAL_DISTANCE);

                                    if (binningTime != null) {
                                        binningCountArray.add(new StepBinningData(binningTime,
                                                binningCount,
                                                binningDistance));
                                    }
                                }
                                StepContent stepContent = DataMapper.convertStepCount(startTime,
                                        stopTime, limit, ret,
                                        binningCountArray);
                                callbackStepHistory.onNewData(stepContent);
                            } finally {
                                healthDatas.close();
                            }
                        }
                    });
        } catch (Exception e) {
            callbackStepHistory.onError(new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR,
                    e.getMessage()));
        }
    }

    private void readStepDistanceHistories(final long startTime, final long stopTime,
                                           final int limit,
                                           TimeInterval timeInterval, String deviceUuid,
                                           @NonNull final InputKit.Result<List<IKValue<Float>>> callback) {
        try {
            final Pair<TimeGroupUnit, Integer> ret =
                    SHealthUtils.convertTimeInterval(timeInterval);
            AggregateRequest request = aggregateStepHistory(startTime,
                    stopTime, ret,
                    deviceUuid);
            mResolver.aggregate(request).setResultListener(
                    new HealthResultHolder.ResultListener<HealthDataResolver.AggregateResult>() {
                @Override
                public void onResult(HealthDataResolver.AggregateResult healthDatas) {
                    List<StepBinningData> binningCountArray = new ArrayList<>();
                    List<IKValue<Float>> distanceList = new ArrayList<IKValue<Float>>();
                    try {
                        for (HealthData data : healthDatas) {
                            String binningTime = data.getString(ALIAS_BINNING_TIME);
                            int binningCount = data.getInt(ALIAS_TOTAL_COUNT);
                            float binningDistance = data.getInt(ALIAS_TOTAL_DISTANCE);

                            if (binningTime != null) {
                                binningCountArray.add(new StepBinningData(binningTime,
                                        binningCount,
                                        binningDistance));
                            }
                        }
                        distanceList = DataMapper.convertStepDistance(ret,
                                limit,
                                binningCountArray);
                    } finally {
                        callback.onNewData(distanceList);
                        healthDatas.close();
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR, e.getMessage()));
        }
    }

    private abstract class StepRequestListener {
        final InputKit.Result resultCallback;

        StepRequestListener(InputKit.Result callback) {
            this.resultCallback = callback;
        }

        void onStepCount(int count) {
        }

        void onStepDistance(float distance) {
        }

        void onDeviceId(String deviceId) {
        }

        void onError(IKResultInfo error) {
            resultCallback.onError(error);
        }
    }
}
