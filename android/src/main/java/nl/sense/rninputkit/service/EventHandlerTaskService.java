package nl.sense.rninputkit.service;

import android.app.ActivityManager;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.erasmus.R;
import nl.sense.rninputkit.modules.health.event.Event;
import nl.sense.rninputkit.service.activity.detector.Constants;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by panjiyudasetya on 7/26/17.
 */

public class EventHandlerTaskService extends HeadlessJsTaskService {
    private static final String TASK_NAME = "EventHandlerTaskService";

    @Override
    public void onCreate() {
        super.onCreate();
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
    public void onDestroy() {
        super.onDestroy();
        if (SDK_INT >= Build.VERSION_CODES.O) stopForeground(true);
        stopSelf();
    }

    public static void sendEvent(@NonNull final Context context, @NonNull Event event) {
        // To see detected event, you can uncomment this to show it on Android notification
        NotificationHelper.createNotification(
                context,
                "New " + event.getTopic() + " detected.",
                new Gson().toJson(event.getSamples()),
                event.getEventId().hashCode()
        );

        // Since we are not sure executed tasks on JS will slowing down the UI or not,
        // it's better for us to prevent any actions while app is in foreground
        // https://facebook.github.io/react-native/docs/headless-js-android#caveats
        if (isAppOnForeground(context)) {
            NotificationHelper.createNotification(
                    context,
                    "Unable to send data",
                    "Discarded this event :\n" + new Gson().toJson(event) + "\nto avoid slow UI",
                    event.getEventId().hashCode());
            return;
        }

        Intent intentService = new Intent(context, EventHandlerTaskService.class);
        intentService.putExtra("data_event", event.toJson());
        ContextCompat.startForegroundService(context, intentService);
    }

    @Override
    protected HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        Bundle extras = intent == null ? null : intent.getExtras();
        WritableMap data = extras == null ? null : Arguments.fromBundle(extras);
        return new HeadlessJsTaskConfig(
                TASK_NAME,
                data,
                TimeUnit.MINUTES.toMillis(5),
                true);
    }

    private static boolean isAppOnForeground(@NonNull Context context) {
        /**
         We need to check if app is in foreground otherwise the app will crash.
         http://stackoverflow.com/questions/8489993/check-android-application-is-in-foreground-or-not
         **/
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) return false;

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) return false;

        final String packageName = context.getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance
                    == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }
}
