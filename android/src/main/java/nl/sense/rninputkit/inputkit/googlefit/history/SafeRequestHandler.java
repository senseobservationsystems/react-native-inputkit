package nl.sense_os.input_kit.googlefit.history;

import androidx.annotation.NonNull;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import nl.sense_os.input_kit.entity.TimeInterval;

public class SafeRequestHandler {
    /**
     * As for minutely request, we should use safe number for maximum datapoints within those
     * period.
     * eg.:
     *  - 24 hours *  1 minute datapoints = 1440 datapoint <- this one would be pretty rare. consider
     *                                                        no one would keep walking or running
     *                                                        for entire 24 hours without a rest
     *  - 24 hours * 10 minute datapoints = 144 datapoint
     *  - 24 hours * 30 minute datapoints = 48 datapoint
     */
    private static final int SAFE_HOURS_NUMBER_FOR_MINUTELY = 24;
    /**
     * As for hourly / daily / weekly interval, we should use maximum datapoints for those period
     * eg.:
     *   - max minutely : when `minuteValue` == 1 minute-interval -> 12 HOURS
     *                    when `minuteValue` >  1 minute-interval -> 24 HOURS
     *   - max hourly   : 1000 hours = 1000 datapoint within hourly period
     *   - max daily    : 1000 days  = 1000 datapoint within the day period
     *   - max weekly   : 1000 days  = 1000 datapoint within the day period
     *
     * 1000 days = 1000 datapoint for daily basis time interval
     */
    private static final int SAFE_HOURS_NUMBER_FOR_HOURLY = 1000;
    private static final int SAFE_DAYS_NUMBER_FOR_DAILY = 1000;


    /**
     * Get safe request of requested start and end date.
     * This is required to avoid 1000++ datapoints error. Through this way, we will create another
     * request chunk per 12 hours with an asumptions that :
     * 12 hours * 1 minute datapoints = 720 datapoint
     * @param startDate Date of start time request
     * @param endDate   Date of end time request
     * @param timeInterval   {@link TimeInterval} that specified by client
     * @return List of pair of start and end time
     */
    public List<Pair<Long, Long>> getSafeRequest(long startDate, long endDate, TimeInterval timeInterval) {
        // Get the time difference between start and end date
        long diffMillis = endDate - startDate;

        // Get the safe hours (i.e the number of hours we can safely retrieve from google fit)
        // based on the time difference between start and end time
        final long safeIntervalInMilliseconds = getSafeIntervalInMilliseconds(timeInterval);

        List<Pair<Long, Long>> request = new ArrayList<>();

        // Check if the difference between Start and End Time is less then safeHours
        if (diffMillis <= safeIntervalInMilliseconds) {
            request.add(Pair.create(startDate, endDate));
            return request;
        }

        long start = startDate;

        while (start < endDate) {
            long relativeEndTime = start + safeIntervalInMilliseconds;

            long spanRelStartTime = getStartOfDay(relativeEndTime);

            if (relativeEndTime > spanRelStartTime) {
                relativeEndTime = spanRelStartTime;
            }

            if (relativeEndTime > endDate) relativeEndTime = endDate;

            request.add(Pair.create(start, relativeEndTime));

            start = relativeEndTime;
        }
        return request;
    }

    /**
     * Get a safe interval in milliseconds for the time interval passed to the function
     * @param timeInterval Given time interval
     * @return The safe number of milliseconds to use as interval
     */
    private long getSafeIntervalInMilliseconds(TimeInterval timeInterval) {
        // TODO: Add unit test for 1001 days
        if (timeInterval.getTimeUnit() == TimeUnit.DAYS) {
            return TimeUnit.DAYS.toMillis(SAFE_DAYS_NUMBER_FOR_DAILY);
        }
        if (timeInterval.getTimeUnit() == TimeUnit.HOURS) {
            return TimeUnit.HOURS.toMillis(SAFE_HOURS_NUMBER_FOR_HOURLY);
        }
        return TimeUnit.HOURS.toMillis(SAFE_HOURS_NUMBER_FOR_MINUTELY);
    }

    /**
     * Get time stamp of begining of the day of the given anchor time.
     * eg.:
     *   when `anchorTime` = '2018-10-01 00:07:00' -> `beginingOfDay`  = '2018-10-01 00:00:00'
     *   when `anchorTime` = '2018-10-01 23:00:12' -> `beginingOfDay`  = '2018-10-01 00:00:00'
     *   and so on
     * @param anchorTime anchor time
     * @return Time of end of day of the anchor time.
     */
    private long getStartOfDay(long anchorTime) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(anchorTime);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
