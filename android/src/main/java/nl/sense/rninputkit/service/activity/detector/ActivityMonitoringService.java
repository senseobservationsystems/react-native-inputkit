package nl.sense.rninputkit.service.activity.detector;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.erasmus.R;
import nl.sense.rninputkit.service.NotificationHelper;
import nl.sense.rninputkit.service.ServiceNotificationCompat;
import com.erasmus.service.scheduler.SchedulerCompat;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import nl.sense_os.input_kit.constant.SampleType.SampleName;

import static android.os.Build.VERSION.SDK_INT;

public class ActivityMonitoringService extends Service {
    /** The entry point for interacting with activity recognition. */
    private ActivityRecognitionClient mActivityRecognitionClient;
    private boolean mIsActivityUpdateRequested = false;
    private ActivityHandler mActivityHandler;

    /** Subscribe activity updates */
    public static void subscribe(@NonNull Context context) {
        setRequestUpdateState(context, true);
        Intent intentService = new Intent(context, ActivityMonitoringService.class);
        intentService.setAction(Constants.SUBSCRIBE_ACTIVITY_UPDATES);
        ContextCompat.startForegroundService(context, intentService);
    }

    /** Unsubscribe activity updates */
    public static void unsubscribe(@NonNull Context context) {
        setRequestUpdateState(context, false);
        Intent intentService = new Intent(context, ActivityMonitoringService.class);
        intentService.setAction(Constants.UNSUBSCRIBE_ACTIVITY_UPDATES);
        ContextCompat.startForegroundService(context, intentService);
    }

    /** Restore state of activity updates */
    public static void restoreActivityState(@NonNull Context context) {
        Intent intentService = new Intent(context, ActivityMonitoringService.class);
        intentService.setAction(Constants.RESTORE_ACTIVITY_UPDATES);
        ContextCompat.startForegroundService(context, intentService);
    }

    /** Proceed detected activity */
    public static void proceedDetectedActivity(@NonNull Context context,
                                               @NonNull @SampleName String activityType) {
        Intent intentService = new Intent(context, ActivityMonitoringService.class);
        intentService.putExtra(ActivityHandler.ACTIVITY_TYPE, activityType);
        intentService.setAction(Constants.NEW_ACTIVITY_DETECTED);
        ContextCompat.startForegroundService(context, intentService);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SchedulerCompat.getInstance(this).onCreate();
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        mActivityHandler = new ActivityHandler(this);

        // Enable notification channel to make activity recognition works on Android O
        if (SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new ServiceNotificationCompat.Builder(this)
                    .channelId(Constants.INPUT_KIT_CHANNEL_ID)
                    .channelName(getString(R.string.name_of_syncing_steps_channel_desc))
                    .iconId(R.mipmap.ic_notif)
                    .content(getString(R.string.title_of_syncing_steps))
                    .build();
            startForeground(Constants.STEP_COUNT_SENSOR_CHANNEL_ID, notification);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (didAskRequestUpdate(this)) {
            SchedulerCompat.getInstance(this).onDestroy();
            SchedulerCompat.getInstance(this).scheduleImmediately();
        } else {
            SchedulerCompat.getInstance(this).cancelSchedules();
            stopService();
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction() == null ? "" : intent.getAction();
        if (action.equals(Constants.RESTORE_ACTIVITY_UPDATES)) {
            requestActivityUpdates();
        } else if ((action.equals(Constants.SUBSCRIBE_ACTIVITY_UPDATES) && !mIsActivityUpdateRequested)) {
            requestActivityUpdates();
        } else if (action.equals(Constants.UNSUBSCRIBE_ACTIVITY_UPDATES)) {
            removeActivityUpdates();
        } else if (action.equals(Constants.NEW_ACTIVITY_DETECTED) && didAskRequestUpdate(this)) {
            mActivityHandler.proceedIntent(intent);
        } else {
            stopService();
        }
        return START_NOT_STICKY;
    }

    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    public void requestActivityUpdates() {
        if (didAskRequestUpdate(this)) {
            mActivityRecognitionClient
                    .requestActivityUpdates(TimeUnit.HOURS.toMillis(2),
                            getActivityDetectionPendingIntent())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            NotificationHelper.createNotification(
                                    ActivityMonitoringService.this,
                                    Constants.ACTIVITY_REPORT_TITLE,
                                    "Successfully request for an activity update. It happens at "
                                            + DateFormat.getInstance().format(new Date()),
                                    5);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            NotificationHelper.createNotification(
                                    ActivityMonitoringService.this,
                                    Constants.ACTIVITY_REPORT_TITLE,
                                    "Failure to request for an activity update. It happens at "
                                            + DateFormat.getInstance().format(new Date()),
                                    6);
                        }
                    });
        }
    }

    /**
     * Removes activity recognition updates using
     * {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}. Registers success and
     * failure callbacks.
     */
    public void removeActivityUpdates() {
        mActivityRecognitionClient
            .removeActivityUpdates(getActivityDetectionPendingIntent())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    NotificationHelper.createNotification(
                            ActivityMonitoringService.this,
                            Constants.ACTIVITY_REPORT_TITLE,
                            "Successfully stopping an activity update. It happens at "
                                    + DateFormat.getInstance().format(new Date()),
                            7);
                    stopService();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    NotificationHelper.createNotification(
                            ActivityMonitoringService.this,
                            Constants.ACTIVITY_REPORT_TITLE,
                            "Failure while stopping an activity update. It happens at "
                                    + DateFormat.getInstance().format(new Date()),
                            8);
                    stopService();
                }
            });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     * @param context current application context
     * @return True when did ask request updates. False otherwise.
     */
    private static boolean didAskRequestUpdate(@NonNull Context context) {
        return ActivityState.getInstance(context).didAskRequestUpdate();
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether activity updates request.
     * @param context current application context
     * @param requestUpdate True if it's successfully request update, False otherwise.
     */
    private static void setRequestUpdateState(@NonNull Context context, boolean requestUpdate) {
        ActivityState.getInstance(context).setRequestUpdateState(requestUpdate);
    }

    private void stopService() {
        if (SDK_INT >= Build.VERSION_CODES.O) stopForeground(true);
        stopSelf();
    }
}
