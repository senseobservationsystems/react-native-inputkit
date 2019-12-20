package nl.sense.rninputkit.inputkit.googlefit.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DataReadResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.Step;
import nl.sense.rninputkit.inputkit.entity.StepContent;

class StepCountHistoryTask extends HistoryTaskFactory<Integer> {
    private DataNormalizer<Integer> normalizer = new DataNormalizer<Integer>() {
        @NonNull
        @Override
        protected void setValueItems(@NonNull IKValue<Integer> currentItem,
                                     @Nullable IKValue<Integer> nextItem,
                                     @NonNull List<IKValue<Integer>> sourceValues) {
            this.setAsInt(currentItem, nextItem, sourceValues);
        }
    };

    private HistoryExtractor<Integer> extractor = new HistoryExtractor<Integer>() {
        @Override
        protected Integer getDataPointValue(@Nullable DataPoint dataPoint) {
            return this.asInt(dataPoint);
        }
    };

    private StepCountHistoryTask(IFitReader fitDataReader,
                                 List<Pair<Long, Long>> safeRequests,
                                 Options options,
                                 DataType dataTypeRequest,
                                 Pair<DataSource, DataType> aggregateType,
                                 OnCompleteListener<Integer> onCompleteListener,
                                 OnFailureListener onFailureListener) {
        super(fitDataReader,
            safeRequests,
            options,
            dataTypeRequest,
            aggregateType,
            onCompleteListener,
            onFailureListener
        );
    }

    @Override
    protected List<IKValue<Integer>> getValues(List<DataReadResponse> responses) {
        if (responses == null) return Collections.emptyList();

        List<IKValue<Integer>> fitValues = new ArrayList<>();
        for (DataReadResponse response : responses) {
            if (!response.getStatus().isSuccess()) continue;

            // extract value history
            List<IKValue<Integer>> values = extractor.extractHistory(response, options.isUseDataAggregation());

            // check data source availability
            if (values.isEmpty()) continue;

            fitValues.addAll(values);
        }
        return normalizer.normalize(options.getStartTime(),
                options.getEndTime(), fitValues, options.getTimeInterval());
    }

    /**
     * Convert input kit integer values into step content
     * @param values    input kit integer values
     * @param startTime start time of content
     * @param endTime   end time of content
     * @return Step content
     */
    public static StepContent toStepContent(List<IKValue<Integer>> values, long startTime, long endTime) {
        List<Step> steps = new ArrayList<>();
        if (values != null) {
            for (IKValue<Integer> value : values) {
                steps.add(new Step(
                        value.getValue(),
                        value.getStartDate().getEpoch(),
                        value.getEndDate().getEpoch())
                );
            }
        }
        return new StepContent(
                true,
                startTime,
                endTime,
                steps
        );
    }

    static class Builder {
        private IFitReader fitDataReader;
        private List<Pair<Long, Long>> safeRequests;
        private Options options;
        private DataType dataTypeRequest;
        private Pair<DataSource, DataType> aggregateType;
        private OnCompleteListener<Integer> onCompleteListener;
        private OnFailureListener onFailureListener;

        Builder withFitDataReader(IFitReader fitDataReader) {
            this.fitDataReader = fitDataReader;
            return this;
        }

        Builder addSafeRequests(List<Pair<Long, Long>> safeRequests) {
            this.safeRequests = safeRequests;
            return this;
        }

        Builder addOptions(Options options) {
            this.options = options;
            return this;
        }

        Builder addDataType(DataType dataTypeRequest) {
            this.dataTypeRequest = dataTypeRequest;
            return this;
        }

        Builder addAggregateSourceType(Pair<DataSource, DataType> aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        Builder addOnCompleteListener(OnCompleteListener<Integer> onCompleteListener) {
            this.onCompleteListener = onCompleteListener;
            return this;
        }

        Builder addOnFailureListener(OnFailureListener onFailureListener) {
            this.onFailureListener = onFailureListener;
            return this;
        }

        private void validate() {
            if (fitDataReader == null)
                throw new IllegalStateException("Fit history must be provided.");
            if (safeRequests == null)
                throw new IllegalStateException("Time requests must be provided.");
            if (dataTypeRequest == null)
                throw new IllegalStateException("Data type request must be provided.");
            if (aggregateType == null)
                throw new IllegalStateException("Aggregate type must be provided.");
            if (options == null)
                throw new IllegalStateException("Options history must be provided.");
        }

        StepCountHistoryTask build() {
            validate();
            return new StepCountHistoryTask(
                    fitDataReader,
                    safeRequests,
                    options,
                    dataTypeRequest,
                    aggregateType,
                    onCompleteListener,
                    onFailureListener
            );
        }
    }
}
