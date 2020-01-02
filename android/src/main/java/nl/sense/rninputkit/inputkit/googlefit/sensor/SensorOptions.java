package nl.sense.rninputkit.inputkit.googlefit.sensor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;

import java.util.concurrent.TimeUnit;

import static nl.sense.rninputkit.inputkit.constant.DataSampling.DEFAULT_SAMPLING_TIME_UNIT;
import static nl.sense.rninputkit.inputkit.constant.DataSampling.DEFAULT_TIME_SAMPLING_RATE;
import static nl.sense.rninputkit.inputkit.googlefit.sensor.Validator.validateDataType;
import static nl.sense.rninputkit.inputkit.googlefit.sensor.Validator.validateSensorListener;

/**
 * Created by panjiyudasetya on 6/15/17.
 */

public class SensorOptions {
    private DataType mDataType;
    private DataSourcesRequest mDataSourcesRequest;
    private int mSamplingRate;
    private TimeUnit mSamplingTimeUnit;
    private OnDataPointListener mSensorListener;

    private SensorOptions(@NonNull DataType dataType,
                          @NonNull DataSourcesRequest dataSourcesRequest,
                          int timeSampling,
                          @NonNull TimeUnit samplingTimeUnit,
                          @NonNull OnDataPointListener sensorListener) {
        mDataType = dataType;
        mDataSourcesRequest = dataSourcesRequest;
        mSamplingRate = timeSampling;
        mSamplingTimeUnit = samplingTimeUnit;
        mSensorListener = sensorListener;
    }

    public DataType getDataType() {
        return mDataType;
    }

    public DataSourcesRequest getDataSourcesRequest() {
        return mDataSourcesRequest;
    }

    public int getSamplingRate() {
        return mSamplingRate;
    }

    public TimeUnit getSamplingTimeUnit() {
        return mSamplingTimeUnit;
    }

    public OnDataPointListener getSensorListener() {
        return mSensorListener;
    }

    public static class Builder {
        private DataType newDataType;
        private DataSourcesRequest newDataSourcesRequest;
        private int newSamplingRate;
        private TimeUnit newSamplingTimeUnit;
        private OnDataPointListener newSensorListener;

        public Builder dataType(@NonNull DataType dataType, int dataSourceType) {
            newDataType = dataType;
            newDataSourcesRequest = new DataSourcesRequest.Builder()
                    .setDataTypes(dataType)
                    .setDataSourceTypes(dataSourceType < 0 ? DataSource.TYPE_RAW : dataSourceType)
                    .build();
            return this;
        }

        public Builder samplingRate(int samplingRate) {
            newSamplingRate = samplingRate;
            return this;
        }

        public Builder samplingTimeUnit(@Nullable TimeUnit samplingTimeUnit) {
            newSamplingTimeUnit = samplingTimeUnit;
            return this;
        }

        public Builder sensorListener(@NonNull OnDataPointListener sensorListener) {
            newSensorListener = sensorListener;
            return this;
        }

        public SensorOptions build() {
            validateDataType(newDataType);
            validateSensorListener(newSensorListener);

            return new SensorOptions(
                    newDataType,
                    newDataSourcesRequest,
                    newSamplingRate == 0 ? DEFAULT_TIME_SAMPLING_RATE : newSamplingRate,
                    newSamplingTimeUnit == null ? DEFAULT_SAMPLING_TIME_UNIT : newSamplingTimeUnit,
                    newSensorListener
            );
        }
    }
}
