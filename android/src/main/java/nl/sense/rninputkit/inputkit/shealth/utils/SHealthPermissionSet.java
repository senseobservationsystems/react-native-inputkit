package nl.sense.rninputkit.inputkit.shealth.utils;

import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;

import java.util.HashSet;
import java.util.Set;

import nl.sense.rninputkit.inputkit.shealth.StepCountReader;

public class SHealthPermissionSet {
    private static SHealthPermissionSet sHealthPermissionSet;
    private final Set<HealthPermissionManager.PermissionKey> stepPermissionSet;
    private final Set<HealthPermissionManager.PermissionKey> sleepPermissionSet;
    private final Set<HealthPermissionManager.PermissionKey> weightPermissionSet;
    private final Set<HealthPermissionManager.PermissionKey> bloodPressurePermissionSet;

    SHealthPermissionSet() {
        stepPermissionSet = createPermissionSet(
                new String[]{
                        HealthConstants.StepCount.HEALTH_DATA_TYPE,
                        StepCountReader.STEP_SUMMARY_DATA_TYPE_NAME
                });
        sleepPermissionSet = createPermissionSet(
                new String[]{HealthConstants.Sleep.HEALTH_DATA_TYPE});
        weightPermissionSet = createPermissionSet(
                new String[]{HealthConstants.Weight.HEALTH_DATA_TYPE});
        bloodPressurePermissionSet = createPermissionSet(
                new String[]{HealthConstants.BloodPressure.HEALTH_DATA_TYPE});
    }

    public static SHealthPermissionSet getInstance() {
        if (sHealthPermissionSet == null) {
            sHealthPermissionSet = new SHealthPermissionSet();
        }
        return sHealthPermissionSet;
    }

    public Set<HealthPermissionManager.PermissionKey> getStepPermissionSet() {
        return stepPermissionSet;
    }

    public Set<HealthPermissionManager.PermissionKey> getSleepPermissionSet() {
        return sleepPermissionSet;
    }

    public Set<HealthPermissionManager.PermissionKey> getWeightPermissionSet() {
        return weightPermissionSet;
    }

    public Set<HealthPermissionManager.PermissionKey> getBloodPressurePermissionSet() {
        return bloodPressurePermissionSet;
    }

    private Set<HealthPermissionManager.PermissionKey> createPermissionSet(String[] dataTypes) {
        Set<HealthPermissionManager.PermissionKey> pmsKeySet = new HashSet<>();
        for (String permissionKey : dataTypes) {
            pmsKeySet.add(new HealthPermissionManager.PermissionKey(
                    permissionKey,
                    HealthPermissionManager.PermissionType.READ)
            );
        }
        return pmsKeySet;
    }
}
