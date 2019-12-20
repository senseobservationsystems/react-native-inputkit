package nl.sense.rninputkit.inputkit.entity;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.List;
import java.util.Objects;

/**
 * Created by panjiyudasetya on 6/15/17.
 */

public class StepContent extends IKValue<List<Step>> {
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    @Expose(serialize = false)
    private boolean isQueryOk;

    public StepContent(boolean isQueryOk,
                       long startDate,
                       long endDate,
                       @NonNull List<Step> steps) {
        super(steps, new DateContent(startDate), new DateContent(endDate));
        this.isQueryOk = isQueryOk;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StepContent that = (StepContent) o;
        return isQueryOk == that.isQueryOk;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), isQueryOk);
    }

    @Override
    public String toString() {
        return "StepContent{"
                + "isQueryOk=" + isQueryOk
                + ", value=" + value
                + ", startDate=" + startDate
                + ", endDate=" + endDate
                + '}';
    }
}
