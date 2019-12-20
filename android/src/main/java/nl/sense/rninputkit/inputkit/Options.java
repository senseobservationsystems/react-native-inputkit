package nl.sense.rninputkit.inputkit;

import nl.sense.rninputkit.inputkit.constant.Interval;
import nl.sense.rninputkit.inputkit.entity.TimeInterval;

import static nl.sense.rninputkit.inputkit.Options.Validator.validateEndTime;
import static nl.sense.rninputkit.inputkit.Options.Validator.validateStartTime;

/**
 * Created by panjiyudasetya on 6/19/17.
 */

public class Options {
    private static final TimeInterval DEFAULT_TIME_INTERVAL = new TimeInterval(Interval.TEN_MINUTE);

    private Long startTime;
    private Long endTime;
    private boolean useDataAggregation;
    private TimeInterval timeInterval;
    private Integer limitation;

    private Options(Long startTime,
                    Long endTime,
                    boolean useDataAggregation,
                    TimeInterval timeInterval,
                    Integer limitation) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.useDataAggregation = useDataAggregation;
        this.timeInterval = timeInterval;
        this.limitation = limitation;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public boolean isUseDataAggregation() {
        return useDataAggregation;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public Integer getLimitation() {
        return limitation;
    }

    public static class Builder {
        private Long newStartTime;
        private Long newEndTime;
        private boolean newUseDataAggregation;
        private TimeInterval newTimeInterval;
        private Integer newLimitation;

        /**
         * Set start time of steps history.
         * @param startTime epoch
         * @return Builder Options Builder
         */
        public Builder startTime(Long startTime) {
            this.newStartTime = startTime;
            return this;
        }

        /**
         * Set end time of steps history.
         * @param endTime epoch
         * @return Builder Options Builder
         */
        public Builder endTime(Long endTime) {
            this.newEndTime = endTime;
            return this;
        }

        /**
         * It will aggregating steps count data history by specific time and time unit.
         * @return Builder Options Builder
         */
        public Builder useDataAggregation() {
            this.newUseDataAggregation = true;
            return this;
        }

        /**
         * If {@link TimeInterval} not provided it will be set to {@link Options#DEFAULT_TIME_INTERVAL}.
         * @param timeInterval time interval
         * @return Builder Options Builder
         */
        public Builder timeInterval(TimeInterval timeInterval) {
            this.newTimeInterval = timeInterval;
            return this;
        }

        /**
         * Set data limitation if required.
         * @param limitation data limitation
         * @return Builder Options Builder
         */
        public Builder limitation(Integer limitation) {
            this.newLimitation = limitation;
            return this;
        }

        public Options build() {
            newStartTime = validateStartTime(newStartTime);
            newEndTime = validateEndTime(newStartTime, newEndTime);

            return new Options(newStartTime,
                    newEndTime,
                    newUseDataAggregation,
                    newTimeInterval == null ? DEFAULT_TIME_INTERVAL : newTimeInterval,
                    (newLimitation == null || newLimitation <= 0) ? null : newLimitation
            );
        }
    }

    static class Validator {
        /**
         * Validate start time value. If lower than 0, it will be set to 0
         * @param startTime epoch
         * @return valid start time
         */
        static long validateStartTime(Long startTime) {
            if (startTime == null) throw new IllegalStateException("Start time should be defined!");
            return startTime < 0 ? 0 : startTime;
        }

        /**
         * Validate end time value. If end time lower than 0, it will be set to 0
         *
         * @param startTime epoch
         * @param endTime epoch
         * @return valid end time
         * @throws IllegalStateException if end time lower than start time
         */
        static long validateEndTime(Long startTime, Long endTime) {
            if (endTime == null) throw new IllegalStateException("End time should be defined!");

            endTime = endTime < 0 ? 0 : endTime;

            if (endTime < startTime) throw new IllegalStateException("End time cannot be lower than start time!");
            return endTime;
        }
    }
}
