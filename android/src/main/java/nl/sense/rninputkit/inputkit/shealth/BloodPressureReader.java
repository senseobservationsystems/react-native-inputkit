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
import nl.sense.rninputkit.inputkit.entity.BloodPressure;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by xedi on 10/9/17.
 * TODO delete this class
 */

public class BloodPressureReader {
    private final HealthDataResolver mResolver;

    public BloodPressureReader(HealthDataStore store) {
        mResolver = new HealthDataResolver(store, null);
    }

    public void readBloodPressure(final long startTime, final long stopTime,
                                  @NonNull final InputKit.Result<List<BloodPressure>> callback) {
        try {
            HealthDataResolver.ReadRequest request = createRequest(startTime, stopTime);
            mResolver.read(request).setResultListener(
                    new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>() {
                        @Override
                        public void onResult(HealthDataResolver.ReadResult healthDatas) {
                            List<BloodPressure> bloodPressureList = new ArrayList<BloodPressure>();
                            try {
                                for (HealthData data : healthDatas) {
                                    Long time = data.getLong(HealthConstants.BloodPressure.START_TIME);
                                    Integer sys = data.getInt(HealthConstants.BloodPressure.SYSTOLIC);
                                    Integer dia = data.getInt(HealthConstants.BloodPressure.DIASTOLIC);
                                    Float mean = data.getFloat(HealthConstants.BloodPressure.MEAN);
                                    Integer pulse = data.getInt(HealthConstants.BloodPressure.PULSE);
                                    String comment = data.getString(HealthConstants.BloodPressure.COMMENT);

                                    BloodPressure bp = new BloodPressure(sys, dia, time);
                                    bp.setMean(mean);
                                    bp.setPulse(pulse);
                                    bp.setComment(comment);
                                    bloodPressureList.add(bp);
                                }
                            } finally {
                                callback.onNewData(bloodPressureList);
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
                .setDataType(HealthConstants.BloodPressure.HEALTH_DATA_TYPE)
                .setProperties(new String[]{HealthConstants.BloodPressure.DEVICE_UUID,
                        HealthConstants.BloodPressure.START_TIME,
                        HealthConstants.BloodPressure.SYSTOLIC,
                        HealthConstants.BloodPressure.DIASTOLIC,
                        HealthConstants.BloodPressure.MEAN,
                        HealthConstants.BloodPressure.PULSE,
                        HealthConstants.BloodPressure.COMMENT})
                .setLocalTimeRange(HealthConstants.BloodPressure.START_TIME, HealthConstants.BloodPressure.TIME_OFFSET,
                        startTime, stopTime)
                .setSort(HealthConstants.BloodPressure.START_TIME, HealthDataResolver.SortOrder.DESC)
                .build();
    }
}
