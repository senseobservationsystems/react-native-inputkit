package nl.sense.rninputkit.inputkit.googlefit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;

import nl.sense.rninputkit.inputkit.constant.SampleType;

public class FitPermissionSet {
    private static FitPermissionSet sPermissionSet;

    FitPermissionSet() { }

    public static FitPermissionSet getInstance() {
        if (sPermissionSet == null) {
            sPermissionSet = new FitPermissionSet();
        }
        return sPermissionSet;
    }

    public FitnessOptions getPermissionsSet(@Nullable String[] sampleTypes) {
        FitnessOptions.Builder builder = FitnessOptions.builder();
        if (sampleTypes != null && sampleTypes.length > 0) {
            for (String sampleType : sampleTypes) {
                createFitnessOptions(sampleType, builder);
            }
        }
        return builder.build();
    }

    private void createFitnessOptions(@NonNull String sampleType,
                                      @NonNull FitnessOptions.Builder builder) {
        if (sampleType.equals(SampleType.STEP_COUNT)) {
            builder.addDataType(
                    DataType.TYPE_STEP_COUNT_DELTA,
                    FitnessOptions.ACCESS_READ
            ).addDataType(
                    DataType.AGGREGATE_STEP_COUNT_DELTA,
                    FitnessOptions.ACCESS_READ
            );
            return;
        }

        if (sampleType.equals(SampleType.DISTANCE_WALKING_RUNNING)) {
            builder.addDataType(
                    DataType.TYPE_DISTANCE_DELTA,
                    FitnessOptions.ACCESS_READ
            ).addDataType(
                    DataType.AGGREGATE_DISTANCE_DELTA,
                    FitnessOptions.ACCESS_READ
            );
            return;
        }

        if (sampleType.equals(SampleType.WEIGHT)) {
            builder.addDataType(
                    DataType.TYPE_WEIGHT,
                    FitnessOptions.ACCESS_READ
            ).addDataType(
                    DataType.AGGREGATE_WEIGHT_SUMMARY,
                    FitnessOptions.ACCESS_READ
            );
        }
    }
}
