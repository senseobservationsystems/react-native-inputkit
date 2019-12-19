package nl.sense.rninputkit.modules;


import android.util.Log;

import com.erasmus.BuildConfig; // TODO IMPORTS
import nl.sense.rninputkit.helper.LoggerFileWriter;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

/**
 * Created by panjiyudasetya on 6/21/17.
 */

public class LoggerBridge extends ReactContextBaseJavaModule {
    private static final String LOGGER_MODULE_NAME = "Logger";
    private static final String TAG = LOGGER_MODULE_NAME;
    private LoggerFileWriter mLogger;

    @SuppressWarnings("unused") // Used by React Native
    public LoggerBridge(ReactApplicationContext reactContext) {
        super(reactContext);
        if (BuildConfig.IS_DEBUG_MODE_ENABLED) {
            mLogger = new LoggerFileWriter(reactContext);
        }
    }

    @Override
    public String getName() {
        return LOGGER_MODULE_NAME;
    }

    @ReactMethod
    @SuppressWarnings("unused")//Used by React Native application
    public void log(String message) {
        if (BuildConfig.IS_DEBUG_MODE_ENABLED && mLogger != null) {
            Log.d(TAG, "[SenseLogger] : " + message);
            mLogger.logEvent(System.currentTimeMillis(), TAG, message);
        }
    }

}
