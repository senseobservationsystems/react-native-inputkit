package nl.sense.rninputkit.inputkit.helper;

import androidx.annotation.NonNull;
import android.util.Pair;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import nl.sense.rninputkit.inputkit.InputKit.Result;
import nl.sense.rninputkit.inputkit.constant.IKStatus;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

/**
 * Created by panjiyudasetya on 6/19/17.
 */

public class InputKitTimeUtils {
    public static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private InputKitTimeUtils() {
    }

    /**
     * Get minute difference between two timestamp values.
     * @param timeStamp1 First timestamp
     * @param timeStamp2 Second timestamp
     * @return A minute difference
     */
    public static long getMinuteDiff(long timeStamp1, long timeStamp2) {
        // Make sure to exclude milliseconds calculation
        timeStamp1 = timeStamp1 / 1000 * 1000;
        timeStamp2 = timeStamp2 / 1000 * 1000;
        return Math.abs(TimeUnit.MILLISECONDS.toMinutes(timeStamp2 - timeStamp1));
    }

    /**
     * Helper function to detect whether given start and end time are overlapping time window.
     * @param startTime Start time
     * @param endTime   End time
     * @param time Time window
     * @return True if overlapping time window, False otherwise.
     */
    public static boolean isOverlappingTimeWindow(long startTime,
                                                  long endTime,
                                                  @NonNull Pair<Long, Long> time) {
        return (startTime < time.first && endTime >= time.first)
                || (startTime < time.second && endTime >= time.second);
    }

    /**
     * Helper function to detect whether given start and end time are within time window or not.
     *
     * @param startTime Start time
     * @param endTime   End time
     * @param time Time window
     * @return True if within time window, False otherwise.
     */
    public static boolean isWithinTimeWindow(long startTime,
                                             long endTime,
                                             @NonNull Pair<Long, Long> time) {
        return isWithinTimeWindow(startTime, time) && isWithinTimeWindow(endTime, time);
    }

    /**
     * Check is a given time within time period or not.
     * @param time1      Timestamp that needs to be checked
     * @param timePeriod Bound of time period
     * @return True if a given time within time period, False otherwise.
     */
    public static boolean isWithinTimeWindow(long time1, @NonNull Pair<Long, Long> timePeriod) {
        return time1 >= timePeriod.first && time1 < timePeriod.second;
    }

    /**
     * Helper function to populate time window based on specific range and {@link TimeInterval}.
     * For instance, to get time window for each ten minute starting from specific time, it can be
     * achieved by call :
     * <pre>{@code
     *
     *     Pair<Long, Long> timeRange = InputKitTimeUtils.populateOneDayRangeBeforeGivenTime(
     *          new Date().getTimeInMillis()
     *     );
     *
     *     List<Pair<Long, Long>> timeWindow = InputKitTimeUtils.populateTimeWindows(
     *          timeRange.first,
     *          timeRange.second,
     *          new TimeInterval({@link nl.sense.rninputkit.inputkit.constant.Interval#TEN_MINUTE})
     *     );
     * }</pre>
     * Then the output should be like (in human readable format) :
     * <pre>{@code
     *
     *     [{"2017-06-13 12:40:00", "2017-06-13 12:50:00"}, {"2017-06-13 12:30:00", "2017-06-13 12:40:00"}]
     * }</pre>
     *
     * @param startTime Start time
     * @param endTime   End time
     * @param interval  {@link TimeInterval}
     * @return Time window
     */
    public static List<Pair<Long, Long>> populateTimeWindows(long startTime,
                                                             long endTime,
                                                             @NonNull TimeInterval interval) {
        validateTimeInput(startTime, endTime);

        List<Pair<Long, Long>> timeWindows = new ArrayList<>();
        while (startTime < endTime) {
            long relativeEndTime = computeTimeWindow(startTime, interval);
            if (relativeEndTime > endTime) relativeEndTime = endTime;
            timeWindows.add(Pair.create(startTime, relativeEndTime));
            startTime = relativeEndTime;
        }
        return timeWindows;
    }

    /**
     * Validate given time period
     *
     * @param startTime Start time
     * @param endTime   End time
     * @param callback  {@link Result} callback which to be handled
     * @return True if valid time period, False otherwise.
     */
    public static boolean validateTimeInput(long startTime,
                                            long endTime,
                                            @NonNull Result callback) {

        if (!isValidTimePeriod(startTime, endTime)) {
            callback.onError(new IKResultInfo(
                    IKStatus.Code.INVALID_REQUEST,
                    "Invalid time period. Start time and end time should be greater than 0!"
            ));
            return false;
        }

        if (!isValidStartTime(startTime, endTime)) {
            callback.onError(new IKResultInfo(
                    IKStatus.Code.INVALID_REQUEST,
                    "Invalid time period. Start time should less or equals than end time!"
            ));
            return false;
        }
        return true;
    }

    /**
     * Helper function to compute time based on {@link TimeInterval}
     *
     * @param anchorTime Anchor time
     * @param interval   {@link TimeInterval}
     * @return Previous time of known end time
     */
    public static long computeTimeWindow(long anchorTime, @NonNull TimeInterval interval) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(anchorTime);

        // set calendar operator based on given time interval
        if (interval.getTimeUnit().equals(TimeUnit.DAYS)) {
            cal.add(Calendar.DAY_OF_MONTH, interval.getValue());
        } else if (interval.getTimeUnit().equals(TimeUnit.HOURS)) {
            cal.add(Calendar.HOUR_OF_DAY, interval.getValue());
        } else if (interval.getTimeUnit().equals(TimeUnit.MINUTES)) {
            cal.add(Calendar.MINUTE, interval.getValue());
        } else
            throw new IllegalStateException("Unsupported Time Interval detected!\n" + interval.toString());

        return cal.getTimeInMillis();
    }

    /**
     * get epoch time of today in current time zone
     */
    public static long getTodayStartTime() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
    }

    /**
     * Helper function to validate input time.
     *
     * @param startTime Given start time
     * @param endTime   Given end time
     * @throws {@link IllegalStateException}
     */
    private static void validateTimeInput(long startTime, long endTime) {
        if (!isValidTimePeriod(startTime, endTime)) {
            throw new IllegalStateException("Start time and end time should be greater than 0!");
        }
        if (!isValidStartTime(startTime, endTime)) {
            throw new IllegalStateException("Start time should less or equals than end time!");
        }
    }

    /**
     * Validate time period.
     *
     * @param startTime Given start time
     * @param endTime   Given end time
     * @return True if valid, false otherwise.
     */
    private static boolean isValidTimePeriod(long startTime, long endTime) {
        return (startTime > 0 && endTime > 0);
    }

    /**
     * Validate start time value.
     *
     * @param startTime Given start time
     * @param endTime   Given end time
     * @return True if valid, false otherwise.
     */
    private static boolean isValidStartTime(long startTime, long endTime) {
        return startTime <= endTime;
    }

    /**
     * Helper function to convert time in human readable format
     *
     * @param stamp Given start time
     *              example output : 02-Oct-2017 12:30:00
     */
    public static String timeStampToString(long stamp) {
        final String TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss";
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(stamp);
        SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT, Locale.US);
        return dateFormat.format(c.getTime());
    }
}
