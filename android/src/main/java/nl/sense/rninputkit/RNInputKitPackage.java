package nl.sense.rninputkit;

import com.erasmus.modules.HealthBridge;
import com.erasmus.modules.LoggerBridge;
import com.erasmus.modules.health.event.EventHandler;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ahmadmuhsin on 5/24/17.
 */

public class RNInputKitPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> modules = new ArrayList<>();
        modules.add(new HealthBridge(reactContext));
        modules.add(new LoggerBridge(reactContext));
        modules.add(new EventHandler(reactContext));
        return modules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }
}
