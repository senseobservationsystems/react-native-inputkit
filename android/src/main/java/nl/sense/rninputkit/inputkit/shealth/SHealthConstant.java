package nl.sense.rninputkit.inputkit.shealth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by xedi on 9/25/17.
 */

public class SHealthConstant {
    public static final String STEP_COUNT = "com.samsung.health.step_count";
    public static final String STEP_DAILY_TREND = "com.samsung.shealth.step_daily_trend";

    public static final int STATUS_CONNECTED = 0;
    public static final int STATUS_DISCONNECTED = 1;
    public static final int STATUS_ERROR = 2;
    public static final int STATUS_ERROR_INIT = 3;

    public static final int PERMISSION_GRANTED = 0;
    public static final int PERMISSION_DENIED = 1;

    public static final long ONE_MINUTE = 60 * 1000;
    public static final long ONE_HOUR = ONE_MINUTE * 60;
    public static final long ONE_DAY = ONE_HOUR * 24;

    public static final String ASLEEP = "asleep";
    public static final String AWAKE = "awake";
    public static final String IN_BED = "inBed";

    public static final List<String> SUPPORTED_DATA_TYPES =
            new ArrayList<>(Arrays.asList("step_count",
                    "step_history",
                    "sleep",
                    "weight",
                    "blood_pressure"));

    public static final String DATE_FORMAT_DAILY = "yyyy-MM-dd";
    public static final String DATE_FORMAT_HOURLY = "yyyy-MM-dd HH";
    public static final String DATE_FORMAT_MINUTELY = "yyyy-MM-dd HH:mm";

}
