package nl.sense.rninputkit.inputkit.status;

import android.text.TextUtils;

/**
 * Created by panjiyudasetya on 10/12/17.
 */

public class IKProviderInfo extends IKResultInfo {

    public IKProviderInfo(int resultCode, String message) {
        super(resultCode);
        this.message = TextUtils.isEmpty(message)
                ? defaultMessage
                : message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
