package nl.sense.rninputkit.helper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import nl.sense_os.input_kit.entity.DateContent; // TODO IMPORTS
import nl.sense_os.input_kit.entity.IKValue; // TODO IMPORTS

/**
 * Created by panjiyudasetya on 10/23/17.
 */

public class ValueConverter {
    private ValueConverter() { }
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static final String START_DATE_PROPS = "startDate";
    private static final String END_DATE_PROPS = "endDate";
    private static final String VALUE_PROPS = "value";
    private static final String EPOCH_PROPS = "timestamp";
    private static final String STRING_PROPS = "formattedString";

    /**
     * Helper function to convert detected value into writable map
     * @param value  Detected value
     * @return {@link WritableMap}
     */
    public static <T> WritableMap toWritableMap(@Nullable IKValue<T> value) {
        WritableMap map = Arguments.createMap();
        if (value == null) return map;

        map.putMap(START_DATE_PROPS, toWritableMap(value.getStartDate()));
        map.putMap(END_DATE_PROPS, toWritableMap(value.getEndDate()));

        Object objValue = value.getValue();
        if (objValue == null) {
            map.putNull(VALUE_PROPS);
        } else {
            if (objValue instanceof Integer) {
                map.putInt(VALUE_PROPS, (Integer) objValue);
            } else if (objValue instanceof Double) {
                map.putDouble(VALUE_PROPS, (Double) objValue);
            } else if (objValue instanceof Float) {
                map.putDouble(VALUE_PROPS, ((Float) objValue).doubleValue());
            } else if (objValue instanceof Long) {
                map.putDouble(VALUE_PROPS, ((Long) objValue).doubleValue());
            } else if (objValue instanceof String) {
                map.putString(VALUE_PROPS, (String) objValue);
            } else if (objValue instanceof List<?>) {
                map = putListToMap((List<?>) objValue, map);
            } else {
                map.putString(VALUE_PROPS, GSON.toJson(objValue));
            }
        }
        return map;
    }

    /**
     * Helper function to convert value list into {@link WritableArray}
     * @param values Detected values
     * @return {@link WritableArray}
     */
    public static <T> WritableArray toWritableArray(@Nullable List<IKValue<T>> values) {
        WritableArray array = Arguments.createArray();
        if (values == null || values.size() == 0) return array;

        for (Object value : values) {
            if (value instanceof IKValue) {
                array.pushMap(toWritableMap((IKValue) value));
            }
        }
        return array;
    }

    /**
     * Helper function to put generic list to data into writable map
     * @param list  Generic object list
     * @param map   {@link WritableMap} target
     */
    private static WritableMap putListToMap(@Nullable List<?> list,
                                     @NonNull WritableMap map) {
        if (list == null || list.isEmpty()) {
            map.putArray(VALUE_PROPS, Arguments.createArray());
            return map;
        }

        WritableArray valueArray = Arguments.createArray();
        Object object = list.get(0);
        if (object instanceof IKValue) {
            for (Object value : list) {
                valueArray.pushMap(toWritableMap((IKValue) value));
            }
            map.putArray(VALUE_PROPS, valueArray);
        } else map.putString(VALUE_PROPS, GSON.toJson(list));

        return map;
    }

    /**
     * Helper function to convert date content into writable map
     * @param dateContent {@link DateContent}
     * @return {@link WritableMap}
     */
    private static WritableMap toWritableMap(@Nullable DateContent dateContent) {
        WritableMap map = Arguments.createMap();
        if (dateContent == null) return map;

        map.putDouble(EPOCH_PROPS, dateContent.getEpoch());
        map.putString(STRING_PROPS, dateContent.getString());
        return map;
    }
}
