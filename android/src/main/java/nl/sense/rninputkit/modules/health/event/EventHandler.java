package nl.sense.rninputkit.modules.health.event;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import nl.sense.rninputkit.modules.LoggerBridge;
import nl.sense.rninputkit.service.EventHandlerTaskService;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.sense_os.input_kit.constant.IKStatus;
import nl.sense_os.input_kit.entity.IKValue;
import nl.sense_os.input_kit.entity.SensorDataPoint;

/**
 * Created by panjiyudasetya on 7/24/17.
 */

// TODO: should this class have process queue?
public class EventHandler extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String EVENT_HANDLER_MODULE_NAME = "EventHandlerBridge";
    private Set<String> mAvailableListeners = new HashSet<>();
    private List<Event> mPendingEvents = new ArrayList<>();
    private Map<String, Callback> mCompletionBlocks = new HashMap<>();

    private LoggerBridge mLogger;
    private ReactContext mReactContext;
    private boolean mIsHostDestroyed;

    public EventHandler(ReactApplicationContext reactContext) {
        super(reactContext);
        mReactContext = reactContext;
        mReactContext.addLifecycleEventListener(this);

        mLogger = new LoggerBridge(reactContext);
        mIsHostDestroyed = false;
    }

    @Override
    public String getName() {
        return EVENT_HANDLER_MODULE_NAME;
    }

    /**
     * Called by JS layer when a listener is ready
     * This method can be called from multiple threads
     */
    @ReactMethod
    @SuppressWarnings("unused")//used by React Native
    public void onListenerReady(String name, Promise promise) {
        mLogger.log(String.format("new listener: %s became available.", name));

        mAvailableListeners.add(name);
        for (Event event : mPendingEvents) {
            if (name.equals(event.getEventName())) {
                emit(event);
            }
        }
        promise.resolve(null);
    }

    /**
     * Called by JS layer when processing event is completed.
     * This method can be called from multiple threads
     */
    @ReactMethod
    @SuppressWarnings("unused")//used by React Native
    public void onEventDidProcessed(String eventId, Promise promise) {
        Callback completionHandler = mCompletionBlocks.get(eventId);
        if (completionHandler == null) {
            // TODO: Notify Error! This should never happen
            return;
        }

        mCompletionBlocks.remove(eventId);
        promise.resolve(null);
        // This callback potentially triggers everything to be stopped and de-allocated.
        completionHandler.invoke();
    }

    /**
     * Called by Headless JS whenever this handler catalyst destroyed.
     * @param eventId       Event Id
     * @param eventName     Event name
     * @param topic         Event topic name
     * @param payload       Payload event in json format
     */
    @ReactMethod
    @SuppressWarnings("unused")//used by React Native
    public void emit(String eventId, String eventName, String topic, String payload, Promise promise) {
        List<IKValue<?>> payloadObjects;
        try {
            Type typeToken = new TypeToken<List<IKValue>>() { }.getType();
            payloadObjects = new Gson().fromJson(payload, typeToken);
        } catch (Exception e) {
            promise.reject(
                    String.valueOf(IKStatus.Code.INVALID_REQUEST),
                    "Payload should be in collection format of IKValue!"
            );
            return;
        }

        emit(new Event.Builder()
                .eventId(eventId)
                .eventName(eventName)
                .topic(topic)
                .samples(payloadObjects)
                .completion(new Callback() {
                    @Override
                    public void invoke(Object... args) {
                        // TODO: Add completion handler if needed.
                    }
                })
                .build()
        );
        promise.resolve(null);
    }

    // Not exposed to JS
    // called by internal classes to emit event from sensor listener
    public static void emit(@NonNull Context context,
                            @NonNull String eventName,
                            @NonNull SensorDataPoint dataPoint,
                            @NonNull Callback completionBlock) {
        EventHandlerTaskService.sendEvent(
                context,
                new Event.Builder()
                        .eventId(ShortCodeGenerator.generateEventID())
                        .eventName(eventName)
                        .topic(dataPoint.getTopic())
                        .samples(dataPoint.getPayload())
                        .completion(completionBlock)
                        .build()
        );
    }

    // Not exposed to JS
    // called by native components such as Health Kit.
    // This method can be called from multiple threads
    private void emit(@NonNull Event event) {
        mLogger.log("Emitting Event : " + event.toJson());

        // TODO: this check might be not sufficient if there are multiple listeners per type of event.
        if (!mAvailableListeners.contains(event.getEventName())) {
            mPendingEvents.add(
                    new Event.Builder()
                            .eventId(ShortCodeGenerator.generateEventID())
                            .eventName(event.getEventName())
                            .topic(event.getTopic())
                            .samples(event.getSamples())
                            .completion(event.getCompletion())
                            .build()
            );
            return;
        }

        mCompletionBlocks.put(event.getEventId(), event.getCompletion());

        if (!mIsHostDestroyed) {
            mReactContext
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(event.getEventName(), event.toWritableMap());
        } else EventHandlerTaskService.sendEvent(mReactContext, event);
    }

    @Override
    public void onHostResume() {
        // Do nothing here, as long as host didn't destroyed, we still have an access
        // into DeviceEventManagerModule.RCTDeviceEventEmitter
        mIsHostDestroyed = false;
    }

    @Override
    public void onHostPause() {
        // Do nothing here, as long as host didn't destroyed, we still have an access
        // into DeviceEventManagerModule.RCTDeviceEventEmitter
    }

    @Override
    public void onHostDestroy() {
        Log.d(EVENT_HANDLER_MODULE_NAME, "onHostDestroy: Prepare initialize event handler state");
        mIsHostDestroyed = true;
    }
}
