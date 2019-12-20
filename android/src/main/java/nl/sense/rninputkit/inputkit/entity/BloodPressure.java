package nl.sense.rninputkit.inputkit.entity;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * Created by xedi on 10/9/17.
 */

public class BloodPressure {
    private static final Gson GSON = new Gson();
    @Expose
    private Integer systolic;
    @Expose
    private Integer diastolic;
    @Expose
    private Float mean;
    @Expose
    private Integer pulse;
    @Expose
    private String comment;
    @Expose
    private DateContent timeRecord;

    public BloodPressure(Integer sys, Integer dia, Long time) {
        this.systolic = sys;
        this.diastolic = dia;
        this.timeRecord = new DateContent(time);
    }

    public Integer getSystolic() {
        return systolic;
    }

    public void setSystolic(Integer systolic) {
        this.systolic = systolic;
    }

    public Integer getDiastolic() {
        return diastolic;
    }

    public void setDiastolic(Integer diastolic) {
        this.diastolic = diastolic;
    }

    public Float getMean() {
        return mean;
    }

    public void setMean(Float mean) {
        this.mean = mean;
    }

    public Integer getPulse() {
        return pulse;
    }

    public void setPulse(Integer pulse) {
        this.pulse = pulse;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public DateContent getTimeRecord() {
        return timeRecord;
    }

    public void setTimeRecord(DateContent timeRecord) {
        this.timeRecord = timeRecord;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return "{"
                + "time: " + timeRecord.getString()
                + "\nsystolic=" + systolic
                + "\n, diastolic=" + diastolic
                + "\n, mean=" + mean
                + "\n, pulse=" + pulse
                + "\n, comment=" + comment
                + "}";
    }
}
