package nl.sense.rninputkit.helper;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

import nl.sense_os.input_kit.entity.BloodPressure; // TODO IMPORTS

/**
 * Created by xedi on 10/16/17.
 */

public class BloodPressureConverter extends DataConverter {

    public WritableArray toWritableMap(@Nullable List<BloodPressure> bloodPressures) {
        WritableArray array = Arguments.createArray();
        if (bloodPressures == null || bloodPressures.isEmpty()) return array;

        for (BloodPressure bp : bloodPressures) {
            array.pushMap(toWritableMap(bp));
        }
        return array;
    }

    private WritableMap toWritableMap(@Nullable BloodPressure bp) {
        WritableMap map = Arguments.createMap();
        if (bp == null) return map;

        map.putMap("time", toWritableMap(bp.getTimeRecord()));
        map.putInt("systolic", bp.getSystolic());
        map.putInt("diastolic", bp.getDiastolic());
        map.putDouble("mean", bp.getMean());
        map.putInt("pulse", bp.getPulse());
        map.putString("comment", bp.getComment());
        return map;
    }
}
