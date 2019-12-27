package nl.sense.rninputkit.inputkit.googlefit.history;

import androidx.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.result.DataReadResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.entity.DateContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;

/**
 * Abstraction historical data extractor from Google Fitness API
 * @param <T> Expected output type
 *
 * Created by panjiyudasetya on 7/5/17.
 */

public abstract class HistoryExtractor<T> {
    private static final String TAG = "HistoryExtractor";

    /**
     * Helper function to extract historical data based on {@link DataReadResponse} and aggregation
     * key
     * @param dataReadResponse      {@link DataReadResponse} history
     * @param useDataAggregation    Set true to aggregate existing data by a bucket of time periods
     * @return {@link List<IKValue<T>>} Input kit values
     */
    public List<IKValue<T>> extractHistory(DataReadResponse dataReadResponse, boolean useDataAggregation) {
        if (useDataAggregation) {
            return historyFromBucket(dataReadResponse.getBuckets());
        } else {
            return historyFromDataSet(dataReadResponse.getDataSets());
        }
    }

    /**
     * Helper function to extract data points history from {@link DataSet}
     * @param dataSet {@link DataSet}
     * @return {@link List<IKValue<T>} Input kit values
     */
    public List<IKValue<T>> historyFromDataSet(@Nullable DataSet dataSet) {
        if (dataSet == null) return Collections.emptyList();
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        List<IKValue<T>> contents = new ArrayList<>();

        for (DataPoint dp : dataSet.getDataPoints()) {
            contents.add(new IKValue<>(
                    getDataPointValue(dp),
                    new DateContent(dp.getStartTime(TimeUnit.MILLISECONDS)),
                    new DateContent(dp.getEndTime(TimeUnit.MILLISECONDS))
            ));
        }
        return contents;
    }

    /**
     * Helper function to extract historical from {@link Bucket}
     * @param buckets {@link List<Bucket>}
     * @return {@link List<IKValue<T>>} Input kit values
     */
    private List<IKValue<T>> historyFromBucket(@Nullable List<Bucket> buckets) {
        if (buckets == null || buckets.isEmpty()) return Collections.emptyList();

        List<IKValue<T>> contents = new ArrayList<>();
        int startFormIndex = 0;
        for (Bucket bucket : buckets) {
            List<DataSet> dataSets = bucket.getDataSets();
            contents.addAll(startFormIndex, historyFromDataSet(dataSets));
            startFormIndex = contents.size();
        }
        return contents;
    }

    /**
     * Helper function to extract data point history from {@link DataSet}
     * @param dataSets {@link DataSet}
     * @return {@link List<IKValue<T>>} Input kit values
     */
    private List<IKValue<T>> historyFromDataSet(@Nullable List<DataSet> dataSets) {
        if (dataSets == null || dataSets.isEmpty()) return Collections.emptyList();

        List<IKValue<T>> contents = new ArrayList<>();
        int startFormIndex = 0;
        for (DataSet dataSet : dataSets) {
            contents.addAll(startFormIndex, historyFromDataSet(dataSet));
            startFormIndex = contents.size();
        }

        return contents;
    }

    /**
     * Get data point value.
     * @param dataPoint Detected value in {@link DataPoint}
     * @return T value with specific type
     */
    protected abstract T getDataPointValue(@Nullable DataPoint dataPoint);

    /**
     * Convert data point as float value
     * @param dataPoint Detected {@link DataPoint} from Fit history
     * @return Float of data point value, otherwise 0.f will be returned.
     * @throws {@link IllegalStateException} when `value.getFormat()` not equals a float
     *          -> 1 means data point value in integer format
     *          -> 2 means data point value in float format
     *          -> 3 means data point value in string format
     */
    public float asFloat(@Nullable DataPoint dataPoint) {
        Value value = getValue(dataPoint);
        return value == null ? 0.f : value.asFloat();
    }

    /**
     * Convert data point as integer value
     * @param dataPoint Detected {@link DataPoint} from Fit history
     * @return Integer of data point value, otherwise 0 will be returned.
     * @throws {@link IllegalStateException} when `value.getFormat()` not equals integer
     *          -> 1 means data point value in integer format
     *          -> 2 means data point value in float format
     *          -> 3 means data point value in string format
     */
    public int asInt(@Nullable DataPoint dataPoint) {
        Value value = getValue(dataPoint);
        return value == null ? 0 : value.asInt();
    }

    /**
     * Convert data point as string value
     * @param dataPoint Detected {@link DataPoint} from Fit history
     * @return String of data point value, otherwise 0 will be returned.
     * @throws {@link IllegalStateException} when `value.getFormat()` not equals string
     *          -> 1 means data point value in integer format
     *          -> 2 means data point value in float format
     *          -> 3 means data point value in string format
     */
    public String asString(@Nullable DataPoint dataPoint) {
        Value value = getValue(dataPoint);
        return value == null ? "" : value.asString();
    }

    /**
     * Get data point value.
     * @param dataPoint Detected {@link DataPoint}
     * @return {@link Value} of detected data point
     */
    @Nullable
    private Value getValue(@Nullable DataPoint dataPoint) {
        if (dataPoint == null || dataPoint.getDataType() == null) return null;

        List<Field> fields = dataPoint.getDataType().getFields();
        if (fields == null || fields.isEmpty()) return null;

        // Usually this fields only contains one row, so we can directly return the value
        return dataPoint.getValue(fields.get(0));
    }
}
