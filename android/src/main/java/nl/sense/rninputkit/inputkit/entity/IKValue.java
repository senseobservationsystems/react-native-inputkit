package nl.sense.rninputkit.inputkit.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Objects;

/**
 * Created by panjiyudasetya on 10/23/17.
 */

public class IKValue<T> {
    protected static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    @Expose
    protected T value;
    @Expose
    protected DateContent startDate;
    @Expose
    protected DateContent endDate;
    @Expose(serialize = false)
    private boolean flagOverlap;

    public IKValue(T value) {
        this.value = value;
    }

    public IKValue(@NonNull T value,
                   @NonNull DateContent startDate,
                   @NonNull DateContent endDate) {
        this.value = value;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public IKValue(@NonNull DateContent startDate,
                   @NonNull DateContent endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public DateContent getStartDate() {
        return startDate;
    }

    public DateContent getEndDate() {
        return endDate;
    }

    public boolean isFlaggedOverlap() {
        return flagOverlap;
    }

    public void setFlagOverlap(boolean flagOverlap) {
        this.flagOverlap = flagOverlap;
    }

    public static int getTotalIntegers(List<IKValue<Integer>> values) {
        if (values == null || values.isEmpty()) return 0;
        int total = 0;
        for (IKValue<Integer> value : values) {
            total += value.getValue();
        }
        return total;
    }

    public static float getTotalFloats(List<IKValue<Float>> values) {
        if (values == null || values.isEmpty()) return 0;
        float total = 0;
        for (IKValue<Float> value : values) {
            total += value.getValue();
        }
        return total;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IKValue<?> ikValue = (IKValue<?>) o;
        return flagOverlap == ikValue.flagOverlap
                && Objects.equals(value, ikValue.value)
                && Objects.equals(startDate, ikValue.startDate)
                && Objects.equals(endDate, ikValue.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, startDate, endDate, flagOverlap);
    }

    @Override
    public String toString() {
        return "IKValue{"
                + "value=" + value
                + ", startDate=" + startDate
                + ", endDate=" + endDate
                + ", flagOverlap=" + flagOverlap
                + '}';
    }
}
