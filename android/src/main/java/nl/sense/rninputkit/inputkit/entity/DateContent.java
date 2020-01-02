package nl.sense.rninputkit.inputkit.entity;

import com.google.gson.annotations.Expose;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by panjiyudasetya on 7/6/17.
 */

public class DateContent {
    private static final String STR_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";
    private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(STR_DATE_FORMAT, Locale.US);
    @Expose
    private long epoch;
    @Expose
    private String string;

    public DateContent(long epoch) {
        this.epoch = epoch;
        this.string = DATE_FORMATTER.format(new Date(epoch));
    }

    public long getEpoch() {
        return epoch;
    }

    public String getString() {
        return string;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateContent)) return false;

        DateContent that = (DateContent) o;

        if (epoch != that.epoch) return false;
        return string != null ? string.equals(that.string) : that.string == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (epoch ^ (epoch >>> 32));
        result = 31 * result + (string != null ? string.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DateContent{"
                + "epoch=" + epoch
                + ", string='" + string + '\''
                + '}';
    }
}
