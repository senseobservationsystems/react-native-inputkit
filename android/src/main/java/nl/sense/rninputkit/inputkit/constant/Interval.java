package nl.sense.rninputkit.inputkit.constant;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by panjiyudasetya on 6/19/17.
 */

public class Interval {
    private Interval() { }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            ONE_WEEK,
            ONE_DAY,
            AN_HOUR,
            HALF_HOUR,
            TEN_MINUTE,
            ONE_MINUTE
    })
    public @interface IntervalName { }
    public static final String ONE_WEEK = "week";
    public static final String ONE_DAY = "day";
    public static final String AN_HOUR = "hour";
    public static final String HALF_HOUR = "halfHour";
    public static final String TEN_MINUTE = "tenMinute";
    public static final String ONE_MINUTE = "oneMinute";
}
