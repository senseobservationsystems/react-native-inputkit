package nl.sense.rninputkit.service.scheduler;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;

import nl.sense.rninputkit.service.scheduler.v14.AlarmCompat;

import java.lang.ref.WeakReference;

import static android.os.Build.VERSION.SDK_INT;

public class SchedulerCompat implements IScheduler {
    private WeakReference<Context> ctxReference;
    private static SchedulerCompat sInstance;

    private SchedulerCompat(@NonNull Context context) {
        ctxReference = new WeakReference<>(context.getApplicationContext());
    }

    public static SchedulerCompat getInstance(@NonNull Context context) {
        if (sInstance == null  || sInstance.ctxReference == null
                || sInstance.ctxReference.get() == null) {
            sInstance = new SchedulerCompat(context);
        }
        return sInstance;
    }

    @Override
    public void scheduleDaily() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerService.scheduleDaily(ctxReference.get());
            return;
        }
        AlarmCompat.getInstance(ctxReference.get()).scheduleDaily();
    }

    @Override
    public void scheduleImmediately() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerService.scheduleImmediately(ctxReference.get());
            return;
        }
        AlarmCompat.getInstance(ctxReference.get()).scheduleImmediately();
    }

    @Override
    public void cancelSchedules() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobSchedulerService.cancelAllSchedule(ctxReference.get());
            return;
        }
        AlarmCompat.getInstance(ctxReference.get()).cancelSchedules();
    }

    @Override
    public void onCreate() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        AlarmCompat.getInstance(ctxReference.get()).onCreate();
    }

    @Override
    public void onDestroy() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) return;
        AlarmCompat.getInstance(ctxReference.get()).onDestroy();
    }
}
