package nl.sense.rninputkit.inputkit.entity;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;

/**
 * Created by xedi on 10/9/17.
 */

public class Weight {
    private static final Gson GSON = new Gson();
    @Expose
    private DateContent timeRecord;
    @Expose
    private Float weight;
    @Expose
    private Integer bodyFat;
    @Expose
    private String comment;

    public Weight(Float weight, Integer bodyFat, long time) {
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.timeRecord = new DateContent(time);
    }

    public DateContent getTimeRecorded() {
        return timeRecord;
    }

    public void setTimeRecorded(DateContent timeRecord) {
        this.timeRecord = timeRecord;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Integer getBodyFat() {
        return bodyFat;
    }

    public void setBodyFat(Integer bodyFat) {
        this.bodyFat = bodyFat;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return "{"
                + "time: " + timeRecord.getString()
                + "\nweight=" + weight
                + "\n, bodyFat=" + bodyFat
                + "\n, comment=" + comment
                + "}";
    }
}
