package nl.sense.rninputkit.inputkit.shealth;

import androidx.annotation.NonNull;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import java.util.ArrayList;
import java.util.List;

import nl.sense.rninputkit.inputkit.InputKit;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.entity.DateContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by xedi on 10/4/17.
 */

public class SleepReader {
    private final HealthDataResolver mResolver;

    public SleepReader(HealthDataStore store) {
        mResolver = new HealthDataResolver(store, null);
    }

    public void readSleep(final long startTime, final long stopTime,
                          @NonNull final InputKit.Result<List<IKValue<String>>> callback) {
        try {
            HealthDataResolver.ReadRequest request = createRequest(startTime, stopTime);
            mResolver.read(request).setResultListener(
                    new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                @Override
                public void onResult(HealthDataResolver.ReadResult healthDatas) {
                    List<IKValue<String>> sleepData = new ArrayList<IKValue<String>>();
                    try {
                        for (HealthData data : healthDatas) {
                            Long goBed = data.getLong(HealthConstants.Sleep.START_TIME);
                            Long wakeUp = data.getLong(HealthConstants.Sleep.END_TIME);
                            IKValue<String> sleep = new IKValue<>(SHealthConstant.ASLEEP,
                                    new DateContent(goBed),
                                    new DateContent(wakeUp));
                            sleepData.add(sleep);
                        }
                    } finally {
                        callback.onNewData(sleepData);
                        healthDatas.close();
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR, e.getMessage()));
        }
    }

    private HealthDataResolver.ReadRequest createRequest(long startTime, long stopTime) {
        return new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Sleep.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.Sleep.DEVICE_UUID,
                        HealthConstants.Sleep.START_TIME,
                        HealthConstants.Sleep.END_TIME,
                        HealthConstants.Sleep.PACKAGE_NAME})
                .setLocalTimeRange(HealthConstants.Sleep.START_TIME,
                        HealthConstants.Sleep.TIME_OFFSET, startTime, stopTime)
                .setSort(HealthConstants.Sleep.START_TIME, HealthDataResolver.SortOrder.DESC)
                .build();
    }
}
