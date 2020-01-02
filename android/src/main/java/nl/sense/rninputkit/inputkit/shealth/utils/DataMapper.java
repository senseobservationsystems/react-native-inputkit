package nl.sense.rninputkit.inputkit.shealth.utils;

import android.util.Pair;

import com.samsung.android.sdk.healthdata.HealthDataResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.sense.rninputkit.inputkit.constant.SampleType;
import nl.sense.rninputkit.inputkit.entity.DateContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.SensorDataPoint;
import nl.sense.rninputkit.inputkit.entity.Step;
import nl.sense.rninputkit.inputkit.entity.StepContent;
import nl.sense.rninputkit.inputkit.shealth.StepBinningData;

/**
 * Created by xedi on 10/19/17.
 */

public final class DataMapper {

    public static StepContent convertStepCount(long startTime, long endTime, int limit,
                                               Pair<HealthDataResolver.AggregateRequest.TimeGroupUnit,
                                                       Integer> interval,
                                               List<StepBinningData> stepBinningDataList) {
        long timeDiff = SHealthUtils.timeDiff();
        long startTimeSrcLocal = startTime - timeDiff;
        long endTimeSrcLocal = endTime - timeDiff;
        long intervalInMillis = SHealthUtils.intervalToMillis(interval);

        List<Step> contents = new ArrayList<>();
        long firstDataTime = startTime;
        if (!stepBinningDataList.isEmpty()) {
            StepBinningData firstData = stepBinningDataList.get(0);
            firstDataTime = SHealthUtils.toMillis(firstData.time, interval.first);
        }
        // to bottom range
        long pivotTimeBott = firstDataTime;
        do {
            contents.add(
                    createStep(stepBinningDataList,
                            interval.first,
                            pivotTimeBott,
                            pivotTimeBott + intervalInMillis));
            pivotTimeBott = pivotTimeBott - intervalInMillis;
        } while (pivotTimeBott > startTimeSrcLocal);
        contents.add(
                createStep(stepBinningDataList,
                        interval.first,
                        pivotTimeBott,
                        pivotTimeBott + intervalInMillis));
        Collections.reverse(contents);
        // to top range
        do {
            firstDataTime = firstDataTime + intervalInMillis;
            contents.add(
                    createStep(stepBinningDataList,
                            interval.first,
                            firstDataTime,
                            firstDataTime + intervalInMillis));
        } while ((firstDataTime + intervalInMillis) < endTimeSrcLocal);

        List<Step> contentLimits = new ArrayList<>();
        for (Step step : contents) {
            if (outOfLimit(contentLimits.size(), limit)) {
                break;
            }
            contentLimits.add(step);
        }
        return new StepContent(
                true,
                startTimeSrcLocal,
                endTimeSrcLocal,
                contentLimits
        );
    }

    public static List<IKValue<Float>>
    convertStepDistance(Pair<HealthDataResolver.AggregateRequest.TimeGroupUnit, Integer> interval,
                        int limit,
                        List<StepBinningData> stepBinningDataList) {
        long intervalInMillis = SHealthUtils.intervalToMillis(interval);

        List<IKValue<Float>> distanceList = new ArrayList<>();
        for (StepBinningData sd : stepBinningDataList) {
            if (outOfLimit(distanceList.size(), limit)) {
                break;
            }
            float distanceValue = sd.distance;
            long time = SHealthUtils.toMillis(sd.time, interval.first);
            IKValue<Float> distance = new IKValue<Float>(distanceValue,
                    new DateContent(time),
                    new DateContent(time + intervalInMillis));
            distanceList.add(distance);
        }
        return distanceList;
    }

    public static SensorDataPoint toSensorDataPoint(String type,
                                                    long startTime,
                                                    int step, float distance) {
        List<IKValue<?>> payloads = new ArrayList<>();
        startTime = startTime - SHealthUtils.timeDiff();
        long endTime = System.currentTimeMillis();
        String topic = "";
        if (type.contains(SampleType.STEP_COUNT)) {
            payloads.add(new IKValue<Integer>(step,
                    new DateContent(startTime),
                    new DateContent(endTime)));
            topic = SampleType.STEP_COUNT;
        }
        if (type.contains(SampleType.DISTANCE_WALKING_RUNNING)) {
            payloads.add(new IKValue<Float>(distance,
                    new DateContent(startTime),
                    new DateContent(endTime)));
            topic = topic + SampleType.DISTANCE_WALKING_RUNNING;
        }
        return new SensorDataPoint(
                topic,
                payloads
        );
    }

    private static boolean outOfLimit(int size, int limit) {
        return (limit > 0 && size >= limit);
    }

    private static Step createStep(List<StepBinningData> stepBinningDataList,
                                   HealthDataResolver.AggregateRequest.TimeGroupUnit tgu,
                                   long time1, long time2) {
        int stepCount = 0;
        for (StepBinningData sd : stepBinningDataList) {
            long time = SHealthUtils.toMillis(sd.time, tgu);
            if (time1 == time) {
                stepCount = sd.count;
                break;
            }
        }
        return new Step(stepCount, time1, time2);
    }
}
