package nl.sense.rninputkit.inputkit.entity;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.constant.Interval;

import static nl.sense.rninputkit.inputkit.constant.Interval.ONE_DAY;
import static nl.sense.rninputkit.inputkit.constant.Interval.HALF_HOUR;
import static nl.sense.rninputkit.inputkit.constant.Interval.AN_HOUR;
import static nl.sense.rninputkit.inputkit.constant.Interval.ONE_MINUTE;
import static nl.sense.rninputkit.inputkit.constant.Interval.TEN_MINUTE;
import static nl.sense.rninputkit.inputkit.constant.Interval.ONE_WEEK;

/**
 * Created by panjiyudasetya on 6/19/17.
 */

public class TimeInterval {
    private int mValue;
    private TimeUnit mTimeUnit;

    public TimeInterval(@NonNull @Interval.IntervalName String type) {
        setValue(type);
    }

    public int getValue() {
        return mValue;
    }

    public TimeUnit getTimeUnit() {
        return mTimeUnit;
    }

    private void setValue(@Interval.IntervalName String type) {
        if (type.equals(ONE_WEEK)) {
            mValue = 7;
            mTimeUnit = TimeUnit.DAYS;
        } else if (type.equals(ONE_DAY)) {
            mValue = 1;
            mTimeUnit = TimeUnit.DAYS;
        } else if (type.equals(AN_HOUR)) {
            mValue = 1;
            mTimeUnit = TimeUnit.HOURS;
        } else if (type.equals(HALF_HOUR)) {
            mValue = 30;
            mTimeUnit = TimeUnit.MINUTES;
        } else if (type.equals(TEN_MINUTE)) {
            mValue = 10;
            mTimeUnit = TimeUnit.MINUTES;
        } else if (type.equals(ONE_MINUTE)) {
            mValue = 1;
            mTimeUnit = TimeUnit.MINUTES;
        } else {
            mValue = 1;
            mTimeUnit = TimeUnit.DAYS;
        }
    }

    @Override
    public String toString() {
        return "TimeInterval{"
                + "value=" + mValue
                + ", timeUnit=" + mTimeUnit
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TimeInterval)) return false;

        TimeInterval that = (TimeInterval) o;

        if (mValue != that.mValue) return false;
        return mTimeUnit == that.mTimeUnit;

    }

    @Override
    public int hashCode() {
        int result = mValue;
        result = 31 * result + (mTimeUnit != null ? mTimeUnit.hashCode() : 0);
        return result;
    }
}
