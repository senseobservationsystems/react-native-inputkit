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
import nl.sense.rninputkit.inputkit.entity.Weight;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by xedi on 10/9/17.
 */

public class WeightReader {
    private final HealthDataResolver mResolver;

    public WeightReader(HealthDataStore store) {
        mResolver = new HealthDataResolver(store, null);
    }

    public void readWeight(final long startTime, final long stopTime,
                           @NonNull final InputKit.Result<List<Weight>> callback) {
        try {
            HealthDataResolver.ReadRequest request = createRequest(startTime, stopTime);
            mResolver.read(request).setResultListener(
                    new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                        @Override
                        public void onResult(HealthDataResolver.ReadResult healthDatas) {
                            List<Weight> weightList = new ArrayList<Weight>();
                            try {
                                for (HealthData data : healthDatas) {
                                    Long time = data.getLong(HealthConstants.Weight.START_TIME);
                                    Float weight = data.getFloat(HealthConstants.Weight.WEIGHT);
                                    Integer bodyFat = data.getInt(HealthConstants.Weight.BODY_FAT);
                                    String comment = data.getString(HealthConstants.Weight.COMMENT);

                                    Weight weightData = new Weight(weight, bodyFat, time);
                                    weightData.setComment(comment);
                                    weightList.add(weightData);
                                }
                            } finally {
                                callback.onNewData(weightList);
                                healthDatas.close();
                            }
                        }
                    });
        } catch (Exception e) {
            callback.onError(
                    new IKResultInfo(IKStatus.Code.UNKNOWN_ERROR,
                            e.getMessage()));
        }
    }

    private HealthDataResolver.ReadRequest createRequest(long startTime, long stopTime) {
        return new HealthDataResolver.ReadRequest.Builder()
                .setDataType(HealthConstants.Weight.HEALTH_DATA_TYPE)
                .setProperties(new String[]{
                        HealthConstants.Weight.DEVICE_UUID,
                        HealthConstants.Weight.START_TIME,
                        HealthConstants.Weight.WEIGHT,
                        HealthConstants.Weight.BODY_FAT,
                        HealthConstants.Weight.COMMENT})
                .setLocalTimeRange(HealthConstants.Weight.START_TIME,
                        HealthConstants.Weight.TIME_OFFSET, startTime, stopTime)
                .setSort(HealthConstants.Weight.START_TIME,
                        HealthDataResolver.SortOrder.DESC)
                .build();
    }
}
