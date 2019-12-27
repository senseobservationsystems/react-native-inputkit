package nl.sense.rninputkit.inputkit.helper;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import nl.sense.rninputkit.inputkit.entity.IKValue;

/**
 * Created by panjiyudasetya on 7/3/17.
 */

public class CollectionUtils {
    /**
     * Helper function to sort steps collections.
     *
     * @param ascending Set to True to use ascending sort,
     *                  False to use descending
     * @param values    Input kit values
     */
    public static <T> void sort(boolean ascending, @NonNull List<IKValue<T>> values) {
        // create comparator based on sorted type
        Comparator<IKValue> comparator;
        if (ascending) {
            comparator = new Comparator<IKValue>() {
                @Override
                public int compare(IKValue ikValue1, IKValue ikValue2) {
                    return compareLong(ikValue1.getStartDate().getEpoch(), ikValue2.getStartDate().getEpoch());
                }
            };
        } else {
            comparator = new Comparator<IKValue>() {
                @Override
                public int compare(IKValue ikValue1, IKValue ikValue2) {
                    return compareLong(ikValue2.getStartDate().getEpoch(), ikValue1.getStartDate().getEpoch());
                }
            };
        }

        Collections.sort(values, comparator);
    }

    /**
     * Helper function to compare two long values
     * @param value1 First value to compare
     * @param value2 Second value to compare
     * @return int result
     */
    @SuppressWarnings("PMD") // Long.compare(value1, value2) is no available on API 16
    private static int compareLong(Long value1, Long value2) {
        return value1.compareTo(value2);
    }
}
