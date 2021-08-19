package nl.sense.rninputkit.service.activity.detector;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import android.util.Pair;

import nl.sense.rninputkit.data.Constants;
import nl.sense.rninputkit.modules.health.event.EventHandler;
import com.facebook.react.bridge.Callback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.sense.rninputkit.inputkit.InputKit; // TODO IMPORTS
import nl.sense.rninputkit.inputkit.entity.DateContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.status.IKResultInfo;

import static nl.sense.rninputkit.data.Constants.JS_SUPPORTED_EVENTS;
import static nl.sense.rninputkit.inputkit.constant.SampleType.DISTANCE_WALKING_RUNNING;
import static nl.sense.rninputkit.inputkit.constant.SampleType.STEP_COUNT; // TODO IMPORTS

public class ActivityHandler {
    public static final String ACTIVITY_TYPE = "activityType";
    public static  final String STEP_DISTANCE_ACTIVITY = STEP_COUNT + "," + DISTANCE_WALKING_RUNNING;
    private Context mContext;

    public ActivityHandler(Context context) {
        this.mContext = context;
    }

    public void proceedIntent(Intent intent) {
        if (intent == null || intent.getExtras() == null) return;


        boolean isStepCountActive = InputKit.getInstance(mContext)
                .isPermissionsAuthorised(new String[]{STEP_COUNT});

        String type = intent.getExtras().getString(ACTIVITY_TYPE, "");
        if (type.equals(STEP_DISTANCE_ACTIVITY)) {
            if (isStepCountActive) {
                emitStepCount();
            }
        }
    }

    private void emitStepCount() {
        final List<IKValue<?>> payloads = new ArrayList<>();
        final Pair<Long, Long> interval = createInterval();
        InputKit.getInstance(mContext).getStepCount(
                new InputKit.Result<Integer>() {
                    @Override
                    public void onNewData(Integer data) {
                        payloads.add(new IKValue<>(
                                data,
                                new DateContent(interval.first),
                                new DateContent(interval.second)
                        ));
                        emit(new SensorDataPoint(STEP_COUNT, payloads));
                    }

                    @Override
                    public void onError(@NonNull IKResultInfo error) { }
                });
    }


    private void emit(final SensorDataPoint dataPoint) {
        // Emit sensor data point to JS
        EventHandler.emit(mContext,
                JS_SUPPORTED_EVENTS.get(Constants.EVENTS.inputKitUpdates),
                dataPoint,
                new Callback() {
                    @Override
                    public void invoke(Object... args) {

                    }
                } // TODO : Does completion callback is necessary?
        );
    }

    private Pair<Long, Long> createInterval() {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return Pair.create(cal.getTimeInMillis(), System.currentTimeMillis());
    }
}
