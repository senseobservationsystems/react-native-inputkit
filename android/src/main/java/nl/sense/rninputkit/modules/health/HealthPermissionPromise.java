package nl.sense.rninputkit.modules.health;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;

import nl.sense.rninputkit.inputkit.status.IKProviderInfo;

/**
 * Created by panjiyudasetya on 11/20/17.
 */

public class HealthPermissionPromise {
    private Promise promise;
    private IKProviderInfo providerInfo;

    public HealthPermissionPromise(@NonNull Promise promise,
                                   @NonNull IKProviderInfo providerInfo) {
        this.promise = promise;
        this.providerInfo = providerInfo;
    }

    public Promise getPromise() {
        return promise;
    }

    public IKProviderInfo getProviderInfo() {
        return providerInfo;
    }
}
