package nl.sense.rninputkit.service.scheduler.v14;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.NonNull;

import nl.sense.rninputkit.service.scheduler.IScheduler;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;
import static android.os.Build.VERSION.SDK_INT;
import static java.util.concurrent.TimeUnit.MINUTES;

public class AlarmCompat implements IScheduler {
    private static final String ACTION_SCHEDULE_ALARM_INTENT = "ACTION_SCHEDULE_ALARM_INTENT";
    private static final int PI_SELF_SCHEDULED_ALARM = 1000;
    private static final int PI_REPEATING_ALARM = 2000;
    private static final AlarmReceiver ALARM_RECEIVER = new AlarmReceiver();

    private WeakReference<Context> ctxReference;
    private AlarmManager mAlarmManager;
    private boolean didRegisterAlarmReceiver;
    private static AlarmCompat sInstance;

    private AlarmCompat(Context context) {
        ctxReference = new WeakReference<>(context.getApplicationContext());
        mAlarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        onCreate();
    }

    public static AlarmCompat getInstance(@NonNull Context context) {
        if (sInstance == null || sInstance.ctxReference == null
                || sInstance.ctxReference.get() == null) {
            sInstance = new AlarmCompat(context);
        }
        return sInstance;
    }

    /**
     * Register alarm receiver
     */
    @Override
    public void onCreate() {
        if (!didRegisterAlarmReceiver) {
            ctxReference.get().registerReceiver(ALARM_RECEIVER, new IntentFilter(ACTION_SCHEDULE_ALARM_INTENT));
            didRegisterAlarmReceiver = true;
        }
    }

    /**
     * Wake up alarm intent immediately.
     */
    @Override
    public void scheduleImmediately() {
        final long fewMinutesFromNow = System.currentTimeMillis() + MINUTES.toMillis(2);
        Intent exactIntent = new Intent(ctxReference.get(), AlarmReceiver.class);
        setAlarm(fewMinutesFromNow, PI_SELF_SCHEDULED_ALARM, exactIntent);
    }

    /**
     * Schedule alarm daily
     */
    @Override
    public void scheduleDaily() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final long firstAlarmTimestamp  = calendar.getTimeInMillis();
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        final long secondAlarmTimestamp = calendar.getTimeInMillis();
        final long oneDayInMillis = TimeUnit.DAYS.toMillis(1);

        Intent alarmIntent = new Intent(ctxReference.get(), AlarmReceiver.class);
        // First alarm must be set at 08:00
        repeatAlarm(firstAlarmTimestamp, oneDayInMillis, PI_REPEATING_ALARM, alarmIntent);
        // Second alarm must be set at 20:00
        repeatAlarm(secondAlarmTimestamp, oneDayInMillis, PI_REPEATING_ALARM + 1, alarmIntent);
    }

    @Override
    public void cancelSchedules() {
        cancelAlarm(PI_SELF_SCHEDULED_ALARM);
        cancelAlarm(PI_REPEATING_ALARM);
        cancelAlarm(PI_REPEATING_ALARM + 1);
    }

    /**
     * Deregister alarm receiver
     */
    @Override
    public void onDestroy() {
        ctxReference.get().unregisterReceiver(ALARM_RECEIVER);
        didRegisterAlarmReceiver = false;
    }

    /**
     * Helper function to fire an alarm at specific time.
     * @param triggerAtMillis       Start alarm in milliseconds
     * @param alarmId               Alarm id
     * @param exactHandlerIntent    Alarm wake up handler intent
     */
    @SuppressWarnings("ObsoleteSdkInt")
    private void setAlarm(long triggerAtMillis,
                          int alarmId,
                          @NonNull Intent exactHandlerIntent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctxReference.get(),
                alarmId,
                exactHandlerIntent,
                FLAG_UPDATE_CURRENT);

        if (SDK_INT >= Build.VERSION_CODES.M) {
            mAlarmManager.setExactAndAllowWhileIdle(
                    RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } else if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mAlarmManager.setExact(
                    RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        } else {
            mAlarmManager.set(
                    RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
            );
        }
    }

    /**
     * Helper function to create repeating alarm intent
     * @param triggerAtMillis           Start alarm in milliseconds
     * @param intervalInMillis          Interval of repeating alarm in milliseconds
     * @param alarmId               Alarm id
     * @param repeatingHandlerIntent    Repeating alarm handler intent
     */
    private void repeatAlarm(long triggerAtMillis,
                             long intervalInMillis,
                             int alarmId,
                             @NonNull Intent repeatingHandlerIntent) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctxReference.get(),
                alarmId,
                repeatingHandlerIntent,
                FLAG_UPDATE_CURRENT);

        mAlarmManager.setRepeating(
                RTC_WAKEUP,
                triggerAtMillis,
                intervalInMillis,
                pendingIntent
        );
    }

    /**
     * Helper function to cancel repeating alarm intent
     * @param alarmId               Alarm id
     */
    private void cancelAlarm(int alarmId) {
        Intent alarmIntent = new Intent(ctxReference.get(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                ctxReference.get(),
                alarmId,
                alarmIntent,
                FLAG_UPDATE_CURRENT);

        mAlarmManager.cancel(pendingIntent);
    }
}
