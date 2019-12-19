package com.erasmus.service.scheduler.v14;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.erasmus.service.EventHandlerTaskService;
import com.erasmus.service.activity.detector.ActivityMonitoringService;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityMonitoringService.restoreActivityState(context);
        EventHandlerTaskService.acquireWakeLockNow(context);
    }
}
