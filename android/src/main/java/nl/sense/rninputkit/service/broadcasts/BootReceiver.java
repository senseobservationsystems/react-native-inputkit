package nl.sense.rninputkit.service.broadcasts;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import nl.sense.rninputkit.service.activity.detector.ActivityState;
import nl.sense.rninputkit.service.scheduler.SchedulerCompat;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityState.getInstance(context).didAskRequestUpdate()) {
            String action = intent == null
                    ? "" : intent.getAction() == null
                    ? "" : intent.getAction();
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                    || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
                SchedulerCompat.getInstance(context).scheduleDaily();
            }
        }
    }
}
