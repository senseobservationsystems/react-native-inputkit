package nl.sense.rninputkit.inputkit.shealth;

/**
 * Created by xedi on 11/16/17.
 */

public class StepBinningData {
    public final int count;
    public final float distance;
    public String time;

    public StepBinningData(String time, int count, float distance) {
        this.time = time;
        this.count = count;
        this.distance = distance;
    }
}
