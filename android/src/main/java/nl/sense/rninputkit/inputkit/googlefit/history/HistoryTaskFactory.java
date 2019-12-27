package nl.sense.rninputkit.inputkit.googlefit.history;

import android.os.AsyncTask;
import android.util.Pair;

import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;

public abstract class  HistoryTaskFactory<T> extends AsyncTask<Void, Integer, List<IKValue<T>>> {
    public interface OnCompleteListener<T> {
        void onComplete(List<IKValue<T>> result);
    }
    public interface OnFailureListener {
        void onFailure(List<Exception> exceptions);
    }

    private IFitReader fitDataReader;
    private List<Pair<Long, Long>> safeRequests;
    private DataType dataTypeRequest;
    private Pair<?, DataType> aggregateType;
    private OnCompleteListener<T> onCompleteListener;
    private OnFailureListener onFailureListener;
    private HistoryResponseSet responseSet;
    protected Options options;

    protected HistoryTaskFactory(IFitReader fitDataReader,
                       List<Pair<Long, Long>> safeRequests,
                       Options options,
                       DataType dataTypeRequest,
                       Pair<?, DataType> aggregateType,
                       OnCompleteListener<T> onCompleteListener,
                       OnFailureListener onFailureListener) {
        this.fitDataReader = fitDataReader;
        this.safeRequests = safeRequests;
        this.options = options;
        this.dataTypeRequest = dataTypeRequest;
        this.aggregateType = aggregateType;
        this.onCompleteListener = onCompleteListener;
        this.onFailureListener = onFailureListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        responseSet = new HistoryResponseSet();
    }

    @Override
    protected List<IKValue<T>> doInBackground(Void... aVoid) {
        for (Pair<Long, Long> request : safeRequests) {
            try {
                TimeInterval intervalAggregator = options.getTimeInterval()
                        .getTimeUnit() == TimeUnit.DAYS
                        ? new TimeInterval(Interval.ONE_DAY)
                        : options.getTimeInterval();
                Pair<Integer, TimeUnit> timeout = intervalAggregator
                        .getTimeUnit() == TimeUnit.DAYS
                        ? Pair.create(150, TimeUnit.SECONDS)
                        : Pair.create(1, TimeUnit.MINUTES);
                DataReadResponse response = Tasks.await(
                    fitDataReader.readHistory(
                        request.first,
                        request.second,
                        options.isUseDataAggregation(),
                        intervalAggregator,
                        dataTypeRequest,
                        aggregateType
                    ), timeout.first, timeout.second);
                responseSet.addResponse(response);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                responseSet.addException(e);
            }
        }

        return getValues(responseSet.responses());
    }

    @Override
    protected void onPostExecute(List<IKValue<T>> results) {
        if (!responseSet.responses().isEmpty() || responseSet.exceptions().isEmpty()) {
            if (onCompleteListener != null) onCompleteListener.onComplete(results);
            return;
        }

        if (onFailureListener != null) onFailureListener.onFailure(responseSet.exceptions());
    }

    /**
     * Get mapped input kit values from data response
     * @param responses Collection of {@link DataReadResponse} if distance sample
     * @return List of distance sample
     */
    protected abstract List<IKValue<T>> getValues(List<DataReadResponse> responses);

    /**
     * Execute task history within thread pool executor
     */
    public void start() {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    class HistoryResponseSet {
        private List<DataReadResponse> responses;
        private List<Exception> exceptions;

        HistoryResponseSet() {
            this.responses = new ArrayList<>();
            this.exceptions = new ArrayList<>();
        }

        void addResponse(DataReadResponse response) {
            responses.add(response);
        }

        void addException(Exception exception) {
            exceptions.add(exception);
        }

        List<DataReadResponse> responses() {
            return responses;
        }

        List<Exception> exceptions() {
            return exceptions;
        }
    }
}