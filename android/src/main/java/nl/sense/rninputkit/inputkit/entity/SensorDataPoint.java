package nl.sense.rninputkit.inputkit.entity;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Created by panjiyudasetya on 10/20/17.
 */

public class SensorDataPoint {
    public String topic;
    public List<IKValue<?>> payload;

    public SensorDataPoint(@NonNull String topic,
                           @NonNull List<IKValue<?>> payload) {
        this.topic = topic;
        this.payload = payload;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<IKValue<?>> getPayload() {
        return payload;
    }

    public void setPayload(List<IKValue<?>> payload) {
        this.payload = payload;
    }
}