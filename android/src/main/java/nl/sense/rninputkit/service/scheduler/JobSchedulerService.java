package nl.sense.rninputkit.service.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import nl.sense.rninputkit.helper.LoggerFileWriter;
import nl.sense.rninputkit.service.activity.detector.ActivityMonitoringService;

import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;

public class JobSchedulerService extends JobService {
    private static final int IMMEDIATELY_JOB_ID = 1;
    private static final int DAILY_JOB_ID = 2;
    private static final long HALF_DAY_INTERVAL = 12 * 60 * 60 * 1000L;

    private static void logEvent(@NonNull Context context,
                                 @NonNull String message) {
        new LoggerFileWriter(context).logEvent(System.currentTimeMillis(),
                JobSchedulerService.class.getName(),
                message);
    }

    private static JobInfo createJobInfo(@NonNull ComponentName componentName, int type) {
        JobInfo.Builder builder;
        switch (type) {
            case DAILY_JOB_ID:
                builder = new JobInfo.Builder(DAILY_JOB_ID, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setRequiresDeviceIdle(false)
                        .setPeriodic(HALF_DAY_INTERVAL);
                break;
            case IMMEDIATELY_JOB_ID :
            default:
                builder = new JobInfo.Builder(IMMEDIATELY_JOB_ID, componentName)
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setRequiresDeviceIdle(false)
                        .setMinimumLatency(1)
                        .setOverrideDeadline(TimeUnit.MINUTES.toMillis(1));
                break;
        }
        return builder.build();
    }

    private static void scheduleEvent(@NonNull Context context,
                                      @NonNull JobInfo jobInfo) {
        JobScheduler scheduler;
        if (SDK_INT >= Build.VERSION_CODES.M) {
            scheduler = context.getSystemService(JobScheduler.class);
        } else {
            scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

        if (scheduler == null) {
            logEvent(context, "Job scheduler is not available");
            return;
        }

        int resultCode = scheduler.schedule(jobInfo);
        if (resultCode == JobScheduler.RESULT_SUCCESS) {
            logEvent(context, "Job scheduled!");
        } else {
            logEvent(context, "Job is not scheduled!");
        }
    }

    private static void cancelAllEvent(@NonNull Context context) {
        JobScheduler scheduler;
        if (SDK_INT >= Build.VERSION_CODES.M) {
            scheduler = context.getSystemService(JobScheduler.class);
        } else {
            scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

        if (scheduler == null) {
            logEvent(context, "Job scheduler is not available");
            return;
        }

        scheduler.cancelAll();
    }

    public static void scheduleDaily(@NonNull Context context) {
        ComponentName componentName = new ComponentName(context, JobSchedulerService.class);
        scheduleEvent(context, createJobInfo(componentName, DAILY_JOB_ID));
    }

    public static void scheduleImmediately(@NonNull Context context) {
        ComponentName componentName = new ComponentName(context, JobSchedulerService.class);
        scheduleEvent(context, createJobInfo(componentName, IMMEDIATELY_JOB_ID));
    }

    public static void cancelAllSchedule(@NonNull Context context) {
        cancelAllEvent(context);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        ActivityMonitoringService.restoreActivityState(this);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}