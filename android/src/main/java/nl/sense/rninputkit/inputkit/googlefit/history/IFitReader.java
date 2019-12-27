package nl.sense.rninputkit.inputkit.googlefit.history;

import androidx.annotation.NonNull;
import android.util.Pair;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;

import nl.sense.rninputkit.inputkit.entity.TimeInterval;

public interface IFitReader {
    /**
     * Read historical data from Fitness API.
     *
     * @param startTime              Start time cumulative distance
     * @param endTime                End time cumulative distance
     * @param useDataAggregation     Set true to aggregate existing data by a bucket of time periods
     * @param timeIntervalAggregator Time Interval for data aggregation
     * @param fitDataType            Fitness data type
     * @param typeAggregator         Pair of aggregator data type.
     *                                  First value must be source of aggregate. eg.
     *                                  Second value must be aggregate value.
     */
    Task<DataReadResponse> readHistory(long startTime,
                                       long endTime,
                                       boolean useDataAggregation,
                                       @NonNull TimeInterval timeIntervalAggregator,
                                       DataType fitDataType,
                                       Pair<?, DataType> typeAggregator);
}
