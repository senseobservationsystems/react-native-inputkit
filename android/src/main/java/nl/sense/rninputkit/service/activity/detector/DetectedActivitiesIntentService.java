/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.sense.rninputkit.service.activity.detector;

import android.app.IntentService;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.SparseIntArray;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 *  IntentService for handling incoming intents that are generated as a result of requesting
 *  activity updates using
 *  {@link com.google.android.gms.location.ActivityRecognitionClient#requestActivityUpdates(long,
 *      android.app.PendingIntent)}.
 */
public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
        SparseIntArray detectedActivitiesMap = toMap(detectedActivities);

        boolean isWalkingDetected = doesRequirementMatch(detectedActivitiesMap,
                DetectedActivity.WALKING, 50);
        boolean isRunningDetected = doesRequirementMatch(detectedActivitiesMap,
                DetectedActivity.RUNNING, 50);

        if (isWalkingDetected || isRunningDetected) {
            ActivityMonitoringService.proceedDetectedActivity(this,
                    ActivityHandler.STEP_DISTANCE_ACTIVITY);
        }
    }

    private SparseIntArray toMap(@Nullable ArrayList<DetectedActivity> detectedActivities) {
        if (detectedActivities == null || detectedActivities.size() == 0) {
            return new SparseIntArray(0);
        }

        SparseIntArray detectedActivitiesMap = new SparseIntArray(detectedActivities.size());
        for (DetectedActivity activity : detectedActivities) {
            detectedActivitiesMap.put(activity.getType(), activity.getConfidence());
        }

        return detectedActivitiesMap;
    }

    private boolean doesRequirementMatch(@NonNull SparseIntArray detectedActivities,
                                         @NonNull Integer expectedActivity,
                                         int expectedConfidenceValue) {
        int actualConfidenceValue = detectedActivities.get(expectedActivity, -1) == -1
                ? 0 : detectedActivities.get(expectedActivity);
        return actualConfidenceValue > expectedConfidenceValue;
    }
}
