package nl.sense.rninputkit.data;

import java.util.EnumMap;

/**
 * Created by kurniaeliazar on 3/20/17.
 */

public class Constants {
    public enum EVENTS {
        actionTrigger, requestSessionId,
        inputKitUpdates, inputKitTracking
    }

    public static final EnumMap<EVENTS, String> JS_SUPPORTED_EVENTS = new EnumMap<>(EVENTS.class);
    static {
        JS_SUPPORTED_EVENTS.put(EVENTS.actionTrigger, "ACTION_DID_TRIGGER");
        JS_SUPPORTED_EVENTS.put(EVENTS.requestSessionId, "REQUEST_VALID_SESSION_ID");
        JS_SUPPORTED_EVENTS.put(EVENTS.inputKitUpdates, "inputKitUpdates");
        JS_SUPPORTED_EVENTS.put(EVENTS.inputKitTracking, "inputKitTracking");
    }

    /** Used by Input Kits */
    public static final int REQUEST_RESOLVE_ERROR = 999;
    public static final int REQ_REQUIRED_PERMISSIONS = 101;
}
