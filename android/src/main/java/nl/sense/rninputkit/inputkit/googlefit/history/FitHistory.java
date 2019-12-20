package nl.sense.rninputkit.inputkit.googlefit.history;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.List;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.InputKit.Result;
import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by panjiyudasetya on 6/15/17.
 */

@SuppressWarnings("SpellCheckingInspection")
public class FitHistory implements IFitReader {
    private Context mContext;
    private SafeRequestHandler mSafeRequestHandler;

    public FitHistory(@NonNull Context context) {
        this.mContext = context;
        this.mSafeRequestHandler = new SafeRequestHandler();
    }

    /**
     * Get total distance of walk within specific options.
     *
     * @param options  {@link Options}
     * @param callback {@link Result} containing number of total distance
     */
    public void getDistance(@NonNull final Options options,
                            @NonNull final Result<Float> callback) {
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        List<Pair<Long, Long>> safeRequests = mSafeRequestHandler.getSafeRequest(options.getStartTime(),
                options.getEndTime(), options.getTimeInterval());
        new DistanceHistoryTask.Builder()
                .withFitDataReader(this)
                .addSafeRequests(safeRequests)
                .addOptions(options)
                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                .addAggregateTypes(Pair.create(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA))
                .addOnCompleteListener(new HistoryTaskFactory.OnCompleteListener<Float>() {
                    @Override
                    public void onComplete(List<IKValue<Float>> result) {
                        callback.onNewData(IKValue.getTotalFloats(result));
                    }
                })
                .addOnFailureListener(new HistoryTaskFactory.OnFailureListener() {
                    @Override
                    public void onFailure(List<Exception> exceptions) {
                        callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                                exceptions.get(0).getMessage()));
                    }
                })
                .build()
                .start();
    }

    /**
     * Get sample distance of walk within specific options.
     *
     * @param options  {@link Options}
     * @param callback {@link Result} containing number of total distance
     */
    public void getDistanceSamples(@NonNull final Options options,
                                   @NonNull final Result<List<IKValue<Float>>> callback) {
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        List<Pair<Long, Long>> safeRequests = mSafeRequestHandler.getSafeRequest(options.getStartTime(),
                options.getEndTime(), options.getTimeInterval());
        new DistanceHistoryTask.Builder()
                .withFitDataReader(this)
                .addSafeRequests(safeRequests)
                .addOptions(options)
                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                .addAggregateTypes(Pair.create(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA))
                .addOnCompleteListener(new HistoryTaskFactory.OnCompleteListener<Float>() {
                    @Override
                    public void onComplete(List<IKValue<Float>> result) {
                        callback.onNewData(applyLimitation(options.getLimitation(), result));
                    }
                })
                .addOnFailureListener(new HistoryTaskFactory.OnFailureListener() {
                    @Override
                    public void onFailure(List<Exception> exceptions) {
                        callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                                exceptions.get(0).getMessage()));
                    }
                })
                .build()
                .start();
    }

    /**
     * Get daily total step count.
     *
     * @param callback {@link Result} containing number of total steps count
     */
    public void getStepCount(@NonNull final Result<Integer> callback) {
        Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(new OnSuccessListener<DataSet>() {
                    @Override
                    public void onSuccess(DataSet dataSet) {
                        List<IKValue<Integer>> contents = new HistoryExtractor<Integer>() {
                            @Override
                            protected Integer getDataPointValue(@Nullable DataPoint dataPoint) {
                                return this.asInt(dataPoint);
                            }
                        }.historyFromDataSet(dataSet);
                        callback.onNewData(IKValue.getTotalIntegers(contents));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                                e.getMessage()));
                    }
                });
    }

    /**
     * Get total steps count of specific range
     *
     * @param options  Steps count options
     * @param callback {@link Result <Integer>} containing number of total steps count
     */
    @SuppressWarnings("unused")//This is a public API
    public void getStepCount(@NonNull final Options options,
                             @NonNull final Result<Integer> callback) {
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        List<Pair<Long, Long>> safeRequests = mSafeRequestHandler.getSafeRequest(options.getStartTime(),
                options.getEndTime(), options.getTimeInterval());
        new StepCountHistoryTask.Builder()
                .withFitDataReader(this)
                .addSafeRequests(safeRequests)
                .addOptions(options)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addAggregateSourceType(Pair.create(getFitStepCountDataSource(), DataType.AGGREGATE_STEP_COUNT_DELTA))
                .addOnCompleteListener(new HistoryTaskFactory.OnCompleteListener<Integer>() {
                    @Override
                    public void onComplete(List<IKValue<Integer>> result) {
                        callback.onNewData(IKValue.getTotalIntegers(result));
                    }
                })
                .addOnFailureListener(new HistoryTaskFactory.OnFailureListener() {
                    @Override
                    public void onFailure(List<Exception> exceptions) {
                        callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                                exceptions.get(0).getMessage()));
                    }
                })
                .build()
                .start();
    }

    /**
     * Get distribution step count history by specific time period.
     * This function should be called within asynchronous process because of
     * reading historical data through {@link Fitness#HistoryApi} will be executed on main
     * thread by default.
     *
     * @param options  Steps count options
     * @param callback {@link Result} containing a set of step content
     */
    @SuppressWarnings("unused")//This is a public API
    public void getStepCountDistribution(@NonNull final Options options,
                                         @NonNull final Result<StepContent> callback) {
        // Invoke the History API to fetch the data with the query and await the result of
        // the read request.
        List<Pair<Long, Long>> safeRequests = mSafeRequestHandler.getSafeRequest(options.getStartTime(),
                options.getEndTime(), options.getTimeInterval());
        new StepCountHistoryTask.Builder()
                .withFitDataReader(this)
                .addSafeRequests(safeRequests)
                .addOptions(options)
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .addAggregateSourceType(Pair.create(getFitStepCountDataSource(), DataType.AGGREGATE_STEP_COUNT_DELTA))
                .addOnCompleteListener(new HistoryTaskFactory.OnCompleteListener<Integer>() {
                    @Override
                    public void onComplete(List<IKValue<Integer>> result) {
                        StepContent content = StepCountHistoryTask.toStepContent(
                                applyLimitation(options.getLimitation(), result),
                                options.getStartTime(), options.getEndTime());
                        callback.onNewData(content);
                    }
                })
                .addOnFailureListener(new HistoryTaskFactory.OnFailureListener() {
                    @Override
                    public void onFailure(List<Exception> exceptions) {
                        callback.onError(new IKResultInfo(IKStatus.Code.INVALID_REQUEST,
                                exceptions.get(0).getMessage()));
                    }
                })
                .build()
                .start();
    }

    /**
     * To make sure that returned step count data exactly the same with GoogleFit App
     * we need to define Google Fit data source
     * @return Google Fit datasource
     */
    private DataSource getFitStepCountDataSource() {
        return new DataSource.Builder()
                .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                .setType(DataSource.TYPE_DERIVED)
                .setStreamName("estimated_steps")
                .setAppPackageName("com.google.android.gms")
                .build();
    }

    @Override
    public synchronized Task<DataReadResponse> readHistory(long startTime,
                                                           long endTime,
                                                           boolean useDataAggregation,
                                                           @NonNull TimeInterval timeIntervalAggregator,
                                                           DataType fitDataType,
                                                           Pair<?, DataType> typeAggregator) {
        DataReadRequest.Builder requestBuilder = new DataReadRequest.Builder();
        if (useDataAggregation) {
            // The data request can specify multiple data types to return, effectively
            // combining multiple data queries into one call.
            // In this example, it's very unlikely that the request is for several hundred
            // data points each consisting of cumulative distance in meters and a timestamp.
            // The more likely scenario is wanting to see how many distance were achieved
            // per day, for several days.
            if (DataSource.class.isInstance(typeAggregator.first)) {
                requestBuilder.aggregate((DataSource) typeAggregator.first, typeAggregator.second);
            } else if (DataType.class.isInstance(typeAggregator.first)) {
                requestBuilder.aggregate((DataType) typeAggregator.first, typeAggregator.second);
            } else {
                throw new IllegalStateException("Unsupported aggregate type");
            }
            // Analogous to a "Group By" in SQL, defines how data should be aggregated.
            // bucketByTime allows for a time span, whereas bucketBySession would allow
            // bucketing by "sessions", which would need to be defined in code.
            requestBuilder.bucketByTime(timeIntervalAggregator.getValue(), timeIntervalAggregator.getTimeUnit());
        } else requestBuilder.read(fitDataType);

        DataReadRequest request = requestBuilder
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .enableServerQueries()
                .build();

        return Fitness.getHistoryClient(mContext, GoogleSignIn.getLastSignedInAccount(mContext))
                .readData(request);
    }

    /**
     * Helper function to apply limitation from Client
     * @param limit Data limitation
     * @param data  Current data result
     * @param <T>   Data type
     * @return Limited data set
     */
    private <T> List<T> applyLimitation(Integer limit, List<T> data) {
        if (limit == null || limit <= 0 || limit > data.size()) return data;
        data.subList(limit, data.size()).clear();
        return data;
    }
}
