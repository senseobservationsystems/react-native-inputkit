package nl.sense.rninputkit.service;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;

import static android.os.Build.VERSION.SDK_INT;

public class ServiceNotificationCompat {
    private ServiceNotificationCompat() { }
    public static class Builder {
        private Context context;
        private String channelId;
        private String channelName;
        private int iconId;
        private String title;
        private String content;

        public Builder(@NonNull Context context) {
            this.context = context;
        }

        public Builder channelId(@NonNull String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder channelName(@NonNull String channelName) {
            this.channelName = channelName;
            return this;
        }

        public Builder iconId(int iconId) {
            this.iconId = iconId;
            return this;
        }

        public Builder title(@Nullable String title) {
            this.title = title;
            return this;
        }

        public Builder content(@Nullable String content) {
            this.content = content;
            return this;
        }

        public Notification build() {
            if (channelId == null) throw new IllegalStateException("Channel ID must be provided.");
            if (channelName == null) throw new IllegalStateException("Channel name must be provided.");

            if (SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId, channelName,
                        NotificationManager.IMPORTANCE_NONE);
                channel.enableLights(false);
                channel.enableVibration(false);
                channel.setShowBadge(false);

                NotificationManager nm = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));
                if (nm != null) nm.createNotificationChannel(channel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                    .setNumber(0)
                    .setSmallIcon(iconId);
            if (!TextUtils.isEmpty(title)) builder.setContentTitle(title);
            if (!TextUtils.isEmpty(content)) builder.setContentText(content);
            return builder.build();
        }
    }
}
