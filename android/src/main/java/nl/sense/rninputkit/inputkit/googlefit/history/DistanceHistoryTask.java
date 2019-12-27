package nl.sense.rninputkit.inputkit.googlefit.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.result.DataReadResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.sense.rninputkit.inputkit.Options;
import nl.sense.rninputkit.inputkit.entity.IKValue;

class DistanceHistoryTask extends HistoryTaskFactory<Float> {
    private DataNormalizer<Float> normalizer = new DataNormalizer<Float>() {
        @NonNull
        @Override
        protected void setValueItems(@NonNull IKValue<Float> currentItem,
                                     @Nullable IKValue<Float> nextItem,
                                     @NonNull List<IKValue<Float>> sourceValues) {
            this.setAsFloat(currentItem, nextItem, sourceValues);
        }
    };

    private HistoryExtractor<Float> extractor = new HistoryExtractor<Float>() {
        @Override
        protected Float getDataPointValue(@Nullable DataPoint dataPoint) {
            return this.asFloat(dataPoint);
        }
    };

    private DistanceHistoryTask(IFitReader fitDataReader,
                                List<Pair<Long, Long>> safeRequests,
                                Options options,
                                DataType dataTypeRequest,
                                Pair<DataType, DataType> aggregateType,
                                OnCompleteListener<Float> onCompleteListener,
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
    protected List<IKValue<Float>> getValues(List<DataReadResponse> responses) {
        if (responses == null) return Collections.emptyList();

        List<IKValue<Float>> fitValues = new ArrayList<>();
        for (DataReadResponse response : responses) {
            if (!response.getStatus().isSuccess()) continue;

            // extract value history
            List<IKValue<Float>> values = extractor.extractHistory(response, options.isUseDataAggregation());

            // check data source availability
            if (values.isEmpty()) continue;

            fitValues.addAll(values);
        }

        // normalise time window
        return normalizer.normalize(options.getStartTime(),
                options.getEndTime(), fitValues, options.getTimeInterval());
    }

    static class Builder {
        private IFitReader fitDataReader;
        private List<Pair<Long, Long>> safeRequests;
        private Options options;
        private DataType dataTypeRequest;
        private Pair<DataType, DataType> aggregateType;
        private OnCompleteListener<Float> onCompleteListener;
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

        Builder addAggregateTypes(Pair<DataType, DataType> aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        Builder addOnCompleteListener(OnCompleteListener<Float> onCompleteListener) {
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

        DistanceHistoryTask build() {
            validate();
            return new DistanceHistoryTask(
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
