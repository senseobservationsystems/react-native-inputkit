package nl.sense.rninputkit.service;


import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import nl.sense.rninputkit.BuildConfig;
import nl.sense.rninputkit.R; // TODO IMPORTS

import nl.sense.rninputkit.helper.LoggerFileWriter;

public class NotificationHelper {

    @SuppressWarnings("unused")//Will be used when its necessary
    public static void createNotification(@NonNull Context context,
                                          @NonNull String title,
                                          @NonNull String content,
                                          int notificationId) {
        if (BuildConfig.IS_NOTIFICATION_DEBUG_ENABLED) {
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context, "InputKit")
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(title)
                            .setContentText(content)
                            .setNumber(0)
                            .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE);
            NotificationManagerCompat.from(context)
                    .notify(notificationId, notificationBuilder.build());
        }

        if (BuildConfig.IS_DEBUG_MODE_ENABLED) {
            new LoggerFileWriter(context).logEvent(System.currentTimeMillis(),
                    String.format("==== %s ====", title),
                    content);
        }
    }
}
