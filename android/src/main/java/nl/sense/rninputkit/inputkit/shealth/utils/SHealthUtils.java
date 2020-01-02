package nl.sense.rninputkit.inputkit.shealth.utils;

import android.util.Pair;

import com.samsung.android.sdk.healthdata.HealthDataResolver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.shealth.SHealthConstant;

/**
 * Created by xedi on 10/19/17.
 */

public final class SHealthUtils {

    public static long toMillis(String dTime, HealthDataResolver.AggregateRequest.TimeGroupUnit timeGroupUnit) {
        SimpleDateFormat sdf;
        String format = SHealthConstant.DATE_FORMAT_MINUTELY;
        if (timeGroupUnit.equals(HealthDataResolver.AggregateRequest.TimeGroupUnit.DAILY)) {
            format = SHealthConstant.DATE_FORMAT_DAILY;
        } else if (timeGroupUnit.equals(HealthDataResolver.AggregateRequest.TimeGroupUnit.HOURLY)) {
            format = SHealthConstant.DATE_FORMAT_HOURLY;
        } else if (timeGroupUnit.equals(HealthDataResolver.AggregateRequest.TimeGroupUnit.MINUTELY)) {
            format = SHealthConstant.DATE_FORMAT_MINUTELY;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        sdf = new SimpleDateFormat(format, Locale.getDefault());
        try {
            cal.setTime(sdf.parse(dTime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal.getTimeInMillis();
    }

    public static long timeDiff() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.ZONE_OFFSET);
    }

    public static long intervalToMillis(Pair<HealthDataResolver.AggregateRequest.TimeGroupUnit, Integer> interval) {
        HealthDataResolver.AggregateRequest.TimeGroupUnit unit = interval.first;
        Integer value = interval.second;
        if (unit.equals(HealthDataResolver.AggregateRequest.TimeGroupUnit.DAILY)) {
            return SHealthConstant.ONE_DAY * value;
        } else if (unit.equals(HealthDataResolver.AggregateRequest.TimeGroupUnit.HOURLY)) {
            return SHealthConstant.ONE_HOUR * value;
        } else if (unit.equals(HealthDataResolver.AggregateRequest.TimeGroupUnit.MINUTELY)) {
            return SHealthConstant.ONE_MINUTE * value;
        }
        return SHealthConstant.ONE_MINUTE * value;
    }


    public static Pair<HealthDataResolver.AggregateRequest.TimeGroupUnit,
            Integer> convertTimeInterval(TimeInterval timeInterval) {
        if (timeInterval.getTimeUnit().equals(TimeUnit.DAYS)) {
            return new Pair<>(HealthDataResolver.AggregateRequest.TimeGroupUnit.DAILY,
                    timeInterval.getValue());
        } else if (timeInterval.getTimeUnit().equals(TimeUnit.HOURS)) {
            return new Pair<>(HealthDataResolver.AggregateRequest.TimeGroupUnit.HOURLY,
                    timeInterval.getValue());
        }
        if (timeInterval.getTimeUnit().equals(TimeUnit.MINUTES)) {
            return new Pair<>(HealthDataResolver.AggregateRequest.TimeGroupUnit.MINUTELY,
                    timeInterval.getValue());
        }
        return new Pair<>(HealthDataResolver.AggregateRequest.TimeGroupUnit.MINUTELY,
                timeInterval.getValue());
    }
}
