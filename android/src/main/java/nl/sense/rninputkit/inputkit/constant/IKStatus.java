package nl.sense.rninputkit.inputkit.constant;

/**
 * Created by panjiyudasetya on 10/12/17.
 */

public class IKStatus {
    private IKStatus() { }
    public static final String INPUT_KIT_DISCONNECTED = "INPUT_KIT_DISCONNECTED";
    public static final String INPUT_KIT_NOT_CONNECTED = "INPUT_KIT_NOT_CONNECTED";
    public static final String INPUT_KIT_SERVICE_NOT_AVAILABLE = "INPUT_KIT_SERVICE_NOT_AVAILABLE";
    public static final String REQUIRED_GOOGLE_FIT_APP = "REQUIRED_GOOGLE_FIT_APP";
    public static final String INPUT_KIT_OUT_OF_DATE_PLAY_SERVICE = "INPUT_KIT_OUT_OF_DATE_PLAY_SERVICE";
    public static final String INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS = "INPUT_KIT_REQUIRED_GRANTED_PERMISSIONS";
    public static final String INPUT_KIT_CONNECTION_ERROR = "INPUT_KIT_CONNECTION_ERROR";
    public static final String INPUT_KIT_NO_DEVICES_SOURCE = "INPUT_KIT_NO_DEVICES_SOURCE";
    public static final String INPUT_KIT_MONITOR_REGISTERED = "INPUT_KIT_MONITOR_ALREADY_REGISTERED";
    public static final String INPUT_KIT_MONITOR_UNREGISTERED = "INPUT_KIT_MONITOR_UNREGISTERED";
    public static final String INPUT_KIT_MONITORING_NOT_AVAILABLE = "INPUT_KIT_MONITORING_NOT_AVAILABLE";
    public static final String INPUT_KIT_UNREACHABLE_CONTEXT =
            String.format("UNREACHABLE_APPLICATION_CONTEXT \n%s %s",
                    "Context was no longer maintained in memory, ",
                    "you might need to re-initiate InputKit instance before use any apis.");

    public abstract class Code {
        public static final int VALID_REQUEST = 0;
        public static final int UNKNOWN_ERROR = -99;
        public static final int IK_NOT_CONNECTED = -3;
        public static final int IK_NOT_AVAILABLE = -4;
        public static final int GOOGLE_FIT_REQUIRED = -5;
        public static final int OUT_OF_DATE_PLAY_SERVICE = -6;
        public static final int INVALID_REQUEST = -7;
        public static final int REQUIRED_GRANTED_PERMISSIONS = -8;
    }
}
