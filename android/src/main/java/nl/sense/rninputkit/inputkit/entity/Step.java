package nl.sense.rninputkit.inputkit.entity;

/**
 * Created by panjiyudasetya on 6/15/17.
 */

public class Step extends IKValue<Integer> {
    public Step(int value, long startDate, long endDate) {
        super(value, new DateContent(startDate), new DateContent(endDate));
    }
}
