package nl.sense.rninputkit.inputkit.googlefit.history;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import nl.sense.rninputkit.inputkit.entity.DateContent;
import nl.sense.rninputkit.inputkit.entity.IKValue;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;
import nl.sense.rninputkit.inputkit.helper.CollectionUtils;

import static nl.sense.rninputkit.inputkit.helper.InputKitTimeUtils.getMinuteDiff;
import static nl.sense.rninputkit.inputkit.helper.InputKitTimeUtils.isOverlappingTimeWindow;
import static nl.sense.rninputkit.inputkit.helper.InputKitTimeUtils.isWithinTimeWindow;
import static nl.sense.rninputkit.inputkit.helper.InputKitTimeUtils.populateTimeWindows;

public abstract class DataNormalizer<T> {

    /**
     * Setup input kit values according to source values.
     * Next item is required to distribute source value into current and the next items
     * in case it overlap both current and next time periods.
     *
     * @param currentItem  Current item input kit value
     * @param nextItem     Next item input kit value
     * @param sourceValues Source values
     */
    protected abstract void setValueItems(
            @NonNull IKValue<T> currentItem,
            @Nullable IKValue<T> nextItem,
            @NonNull List<IKValue<T>> sourceValues);

    /**
     * Normalize input kit values time window.
     *
     * @param values    Input kit values
     * @param interval  {@link TimeInterval}
     * @return Step history within proper time windows.
     */
    @NonNull
    public List<IKValue<T>> normalize(long startTime,
                                      long endTime,
                                      @NonNull List<IKValue<T>> values,
                                      @NonNull TimeInterval interval) {
        // populate proper time windows
        List<Pair<Long, Long>> timeWindows = populateTimeWindows(
                startTime,
                endTime,
                interval
        );

        // make sure to sort input kit values ascending
        CollectionUtils.sort(true, values);
        List<IKValue<T>> ikValues = populateIKValues(timeWindows);

        // setup input kit values
        setupIKValues(ikValues, values);
        return ikValues;
    }

    /**
     * Populate proper time period for input kit values.
     *
     * @param timeWindows   Time windows
     * @return Input kit values with proper time period
     */
    @NonNull
    private List<IKValue<T>> populateIKValues(@NonNull List<Pair<Long, Long>> timeWindows) {
        List<IKValue<T>> results = new ArrayList<>();
        for (Pair<Long, Long> normalizedTimeWindow : timeWindows) {
            results.add(new IKValue<T>(
                    new DateContent(normalizedTimeWindow.first),
                    new DateContent(normalizedTimeWindow.second))
            );
        }
        return results;
    }

    /**
     * Setup input kit values according to source values
     * @param ikValues      Input kit values within proper time period
     * @param sourceValues  Source values
     */
    private void setupIKValues(@NonNull List<IKValue<T>> ikValues,
                               @NonNull List<IKValue<T>> sourceValues) {
        for (int i = 0; i < ikValues.size(); i++) {
            IKValue<T> currentItem = ikValues.get(i);
            IKValue<T> nextItem = i == ikValues.size() - 1
                    ? null : ikValues.get(i + 1);
            setValueItems(currentItem, nextItem, sourceValues);
        }
    }

    /**
     * Get pair of time period of current item and the next item.
     *
     * @param currentItem  Current item input kit value
     * @param nextItem     Next item input kit value
     * @return Pair of time period of current item and the next item
     */
    private TimePeriod getPairTimePeriod(
            @NonNull IKValue<?> currentItem,
            @Nullable IKValue<?> nextItem) {
        Pair<Long, Long> currentTimePeriod = Pair.create(currentItem.getStartDate().getEpoch(),
                currentItem.getEndDate().getEpoch());
        Pair<Long, Long> nextTimePeriod = nextItem == null
                ? null
                : Pair.create(nextItem.getStartDate().getEpoch(), nextItem.getEndDate().getEpoch());
        return new TimePeriod(currentTimePeriod, nextTimePeriod);
    }

    /**
     * Set current and next item value according to source values in float data type.
     *
     * @param currentItem  Current item input kit value
     * @param nextItem     Next item input kit value
     * @param sourceValues Source values
     */
    protected void setAsFloat(
            @NonNull IKValue<Float> currentItem,
            @Nullable IKValue<Float> nextItem,
            @NonNull List<IKValue<Float>> sourceValues) {
        TimePeriod timePeriod = getPairTimePeriod(currentItem, nextItem);

        // Get pair of overlap values.
        // First item will be added to current item, second value will be added to the next value.
        ValueItems valueItems = getValuePair(timePeriod.currentPeriod,
                timePeriod.nexTimePeriod, sourceValues);

        // Setup current value.
        Float value = currentItem.getValue();
        float incomingValue = valueItems.current.floatValue();
        currentItem.setValue(value == null
            ? incomingValue
            : (value + incomingValue));

        if (nextItem != null) {
            currentItem.setValue(currentItem.getValue() + valueItems.floatOffset);
            nextItem.setValue(valueItems.next.floatValue());
        }
    }

    /**
     * Set current and next item value according to source values in integer data type.
     *
     * @param currentItem  Current item input kit value
     * @param nextItem     Next item input kit value
     * @param sourceValues Source values
     */
    protected void setAsInt(
            @NonNull IKValue<Integer> currentItem,
            @Nullable IKValue<Integer> nextItem,
            @NonNull List<IKValue<Integer>> sourceValues) {
        TimePeriod timePeriod = getPairTimePeriod(currentItem, nextItem);

        // Get pair of overlap values.
        // First item will be added to current item, second value will be added to the next value.
        ValueItems valueItems = getValuePair(timePeriod.currentPeriod,
                timePeriod.nexTimePeriod, sourceValues);

        // Setup current value.
        Integer value = currentItem.getValue();
        int incomingVal = Math.round(valueItems.current.floatValue());
        currentItem.setValue(value == null
                ? incomingVal
                : (value + incomingVal));

        if (nextItem != null) {
            currentItem.setValue(currentItem.getValue() + valueItems.intOffset);
            nextItem.setValue(Math.round(valueItems.next.floatValue()));
        }
    }

    /**
     * Get value for current and next value item according to source values.
     *
     * @param currentTimePeriod  Current time period input kit value
     * @param nextTimePeriod     Next time period of input kit value
     * @param sourceValues Source values
     * @return Pair of total value source.
     *          - First value is a total of source values if it's completely inside time period
     *              of current item.
     *          - Second value is distributed source values if it's overlap current time period or next item
     *              time period as well.
     */
    private <X extends Number> ValueItems getValuePair(
            @NonNull Pair<Long, Long> currentTimePeriod,
            @Nullable Pair<Long, Long> nextTimePeriod,
            @NonNull List<IKValue<X>> sourceValues) {
        Number totalValue = 0, nextValue = 0, actualValue = 0;
        for (IKValue<X> value : sourceValues) {
            Pair<Long, Long> valueTimePeriod = Pair.create(value.getStartDate().getEpoch(),
                    value.getEndDate().getEpoch());

            // Stop counting if value time period exceed end time of the next item time period.
            if (nextTimePeriod != null && valueTimePeriod.second > nextTimePeriod.second) {
                break;
            }

            // Sum up current total value with source value when it still completely within time period.
            if (isWithinTimeWindow(valueTimePeriod.first, valueTimePeriod.second, currentTimePeriod)) {
                totalValue  = sumValues(totalValue,  value.getValue());
                actualValue = sumValues(actualValue, value.getValue());
                continue;
            }

            // Distribute value source to current and the next item when it's overlap.
            if (isOverlappingTimeWindow(valueTimePeriod.first, valueTimePeriod.second, currentTimePeriod)
                    && !value.isFlaggedOverlap()) {
                Pair<Float, Float> overlappingValuePair = getOverlappingValuePair(currentTimePeriod, value);
                totalValue  = sumValues(totalValue,  overlappingValuePair.first);
                actualValue = sumValues(actualValue, value.getValue());
                nextValue   = overlappingValuePair.second;
                value.setFlagOverlap(true);
                break;
            }
        }
        return new ValueItems(totalValue, nextValue, actualValue);
    }

    /**
     * Distribute value among current and the next item when source value item overlap those.
     *
     * @param currentTimePeriod  Current time period of input kit value
     * @param sourceValue  Source value item
     * @return Pair of overlapping value.
     *          First value is a total value for current item.
     *          Second value is an overlap value for the next item.
     */
    private <X extends Number> Pair<Float, Float> getOverlappingValuePair(
            @NonNull Pair<Long, Long> currentTimePeriod,
            @NonNull IKValue<X> sourceValue) {
        // Get specific source value overlapping item information
        Pair<Long, Long> sourceTimePeriod = Pair.create(sourceValue.getStartDate().getEpoch(),
                sourceValue.getEndDate().getEpoch());
        boolean isStartWithinTimePeriod = isWithinTimeWindow(sourceTimePeriod.first, currentTimePeriod);
        boolean isEndWithinTimePeriod = isWithinTimeWindow(sourceTimePeriod.second, currentTimePeriod);

        // It means : Source value end time exceed an end time of current time period
        // In this case, we distribute `right`-extra-value to the next input kit item
        //
        // eg.
        //  - current time period : 08.00 - 08.10
        //  - source time period  : 08.00 - 08.11
        if (isStartWithinTimePeriod && !isEndWithinTimePeriod
                && sourceTimePeriod.second >= currentTimePeriod.second) {
            return getValuePair(sourceValue, currentTimePeriod.second, sourceTimePeriod);
        }

        // It means : Source value `start-time` was below of `start-time` of the current time period
        // In this case, we exclude `left`-extra-value and calculate average value within time period
        // to the current input kit item
        //
        // eg.
        //  - current time period : 08.00 - 08.10
        //  - source time period  : 07.58 - 08.08
        if (!isStartWithinTimePeriod && sourceTimePeriod.first < currentTimePeriod.first
                && isEndWithinTimePeriod) {
            Pair<Float, Float> valuePair = getValuePair(sourceValue,
                    currentTimePeriod.first, sourceTimePeriod);
            return Pair.create(valuePair.second, 0f);
        }

        // It means : Time period was completely within source value `time-window`. In this case,
        // we only calculate value for intersects `time-window` of source value and current time
        // period. Then those calculated value will be distributed to the current input kit item.
        //
        // eg.
        //  - current time period : 08.00 - 08.10
        //  - source time period  : 07.58 - 08.18
        if (!isStartWithinTimePeriod && sourceTimePeriod.first < currentTimePeriod.first
                && !isEndWithinTimePeriod && sourceTimePeriod.second >= currentTimePeriod.second) {
            float srcAvgPerMinute = averageValuePerMinute(sourceValue);
            long timePeriodMinDiff = getMinuteDiff(currentTimePeriod.second, currentTimePeriod.first);
            return Pair.create(srcAvgPerMinute * timePeriodMinDiff, 0f);
        }

        return Pair.create(0f, 0f);
    }

    /**
     * Get left-right step count value distribution per minute.
     * @param sourceValue       Source value
     * @param anchorTime        Anchor time
     * @param sourceTimePeriod  Source time period
     * @return Pair of left and right overlap value.
     */
    private <X extends Number> Pair<Float, Float> getValuePair(
            @NonNull IKValue<X> sourceValue,
            long anchorTime,
            @NonNull Pair<Long, Long> sourceTimePeriod) {
        float srcAvgPerMinute = averageValuePerMinute(sourceValue);
        long leftMinDiff = getMinuteDiff(sourceTimePeriod.first, anchorTime);
        long rightMinDiff = getMinuteDiff(anchorTime, sourceTimePeriod.second);
        if (leftMinDiff == 0 && rightMinDiff == 0) {
            // In this case, source time period overlap anchor time within milliseconds.
            // Then distribute average value into current item of input kit value.
            return Pair.create(srcAvgPerMinute, 0f);
        }
        return Pair.create(srcAvgPerMinute * leftMinDiff, srcAvgPerMinute * rightMinDiff);
    }

    /**
     * Sum values in a number data type
     *
     * @param previous   Previous value
     * @param current    Current value
     * @return Sum of previous and current value if data type recognised.
     *          Otherwise, previous value will be returned.
     */
    private <X extends Number> Number sumValues(X previous, Number current) {
        if (current instanceof Long) {
            return previous.longValue() + current.longValue();
        }
        if (current instanceof Float) {
            return previous.floatValue() + current.floatValue();
        }
        if (current instanceof Integer) {
            return previous.intValue() + current.intValue();
        }
        if (current instanceof Double) {
            return previous.doubleValue() + current.doubleValue();
        }
        if (current instanceof Short) {
            return previous.shortValue() + current.shortValue();
        }
        if (current instanceof Byte) {
            return previous.byteValue() + current.byteValue();
        }
        return previous;
    }

    /**
     * Average value per minutes
     *
     * @param sourceValue     Source value
     * @return Average value per minute if data type recognised.
     *          Otherwise, default value (1L) will be returned.
     */
    private <X extends Number> float averageValuePerMinute(IKValue<X> sourceValue) {
        float minDiff = getMinuteDiff(sourceValue.getEndDate().getEpoch(),
                sourceValue.getStartDate().getEpoch());
        minDiff = minDiff == 0f ? 1f : minDiff;
        Number value = sourceValue.getValue();
        if (value instanceof Long) {
            return value.longValue() / minDiff;
        }
        if (value instanceof Float) {
            return value.floatValue() / minDiff;
        }
        if (value instanceof Integer) {
            return value.intValue() / minDiff;
        }
        if (value instanceof Double) {
            return (float) value.doubleValue() / minDiff;
        }
        if (value instanceof Short) {
            return value.shortValue() / minDiff;
        }
        if (value instanceof Byte) {
            return value.byteValue() / minDiff;
        }
        return 1f;
    }

    class ValueItems {
        private Number current;
        private Number next;
        private Number actual;
        private int    intOffset = 0;
        private float  floatOffset = 0.f;

        ValueItems(Number current, Number next, Number actual) {
            this.current = current;
            this.next = next;
            this.actual = actual;
            calculateOffset();
        }

        private void calculateOffset() {
            this.intOffset = actual.intValue()
                    - (Math.round(current.floatValue()) + Math.round(next.floatValue()));
            this.floatOffset = actual.floatValue() - (current.floatValue() + next.floatValue());
        }
    }

    class TimePeriod {
        private Pair<Long, Long> currentPeriod;
        private Pair<Long, Long> nexTimePeriod;

        TimePeriod(Pair<Long, Long> currentPeriod, Pair<Long, Long> nexTimePeriod) {
            this.currentPeriod = currentPeriod;
            this.nexTimePeriod = nexTimePeriod;
        }
    }
}
