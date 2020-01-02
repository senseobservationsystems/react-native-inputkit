package nl.sense.rninputkit.service.activity.detector;

import android.content.Context;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

public class ActivityState {
    private WeakReference<Context> ctxReference;
    private static ActivityState sInstance;

    private ActivityState(Context context) {
        ctxReference = new WeakReference<>(context);
    }

    public static ActivityState getInstance(@NonNull Context context) {
        if (sInstance == null || sInstance.ctxReference == null
                || sInstance.ctxReference.get() == null) {
            sInstance = new ActivityState(context);
        }
        return sInstance;
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     * @return True when did ask request updates. False otherwise.
     */
    public boolean didAskRequestUpdate() {
        return PreferenceManager.getDefaultSharedPreferences(ctxReference.get())
                .getBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether activity updates request.
     * @param requestUpdate True if it's successfully request update, False otherwise.
     */
    public void setRequestUpdateState(boolean requestUpdate) {
        PreferenceManager.getDefaultSharedPreferences(ctxReference.get())
                .edit()
                .putBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, requestUpdate)
                .apply();
    }
}
