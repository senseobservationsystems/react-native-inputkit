package nl.sense.rninputkit.helper;

import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import nl.sense_os.input_kit.entity.DateContent;

/**
 * Created by xedi on 10/13/17.
 */

public class DataConverter {
    private static final String EPOCH_PROPS = "timestamp";
    private static final String STRING_PROPS = "formattedString";
    /**
     * Helper function to convert date content into writable map
     * @param dateContent {@link DateContent}
     * @return {@link WritableMap}
     */
    protected WritableMap toWritableMap(@Nullable DateContent dateContent) {
        WritableMap map = Arguments.createMap();
        if (dateContent == null) return map;

        map.putDouble(EPOCH_PROPS, dateContent.getEpoch());
        map.putString(STRING_PROPS, dateContent.getString());
        return map;
    }
}
