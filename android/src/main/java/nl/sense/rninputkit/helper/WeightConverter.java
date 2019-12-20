package nl.sense.rninputkit.helper;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.List;

import nl.sense.rninputkit.inputkit.entity.Weight; // TODO IMPORTS

/**
 * Created by xedi on 10/13/17.
 */

public class WeightConverter extends DataConverter {

    public WritableArray toWritableMap(@Nullable List<Weight> weightList) {
        WritableArray array = Arguments.createArray();
        if (weightList == null || weightList.isEmpty()) return array;

        for (Weight weight : weightList) {
            array.pushMap(toWritableMap(weight));
        }
        return array;
    }

    private WritableMap toWritableMap(@Nullable Weight weight) {
        WritableMap map = Arguments.createMap();
        if (weight == null) return map;

        map.putMap("time", toWritableMap(weight.getTimeRecorded()));
        map.putDouble("weight", weight.getWeight());
        map.putInt("bodyFat", weight.getBodyFat());
        map.putString("comment", weight.getComment());
        return map;
    }
}
