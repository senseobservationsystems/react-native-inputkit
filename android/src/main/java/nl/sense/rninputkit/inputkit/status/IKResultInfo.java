package nl.sense.rninputkit.inputkit.status;

import androidx.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by panjiyudasetya on 10/12/17.
 */

public class IKResultInfo {
    protected final String defaultMessage = "UNKNOWN_RESULT_INFO";
    protected int resultCode = 0;
    protected String message;

    public IKResultInfo(int resultCode) {
        this.resultCode = resultCode;
        this.message = defaultMessage;
    }

    public IKResultInfo(int resultCode, @NonNull String message) {
        this.resultCode = resultCode;
        this.message = TextUtils.isEmpty(message)
            ? defaultMessage
            : message;
    }

    public int getResultCode() {
        return resultCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "IKResultInfo{"
                + "message='" + message + '\''
                + ", resultCode=" + resultCode
                + '}';
    }
}
