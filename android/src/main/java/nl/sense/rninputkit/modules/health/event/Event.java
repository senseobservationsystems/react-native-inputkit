package nl.sense.rninputkit.modules.health.event;

import android.os.Bundle;
import androidx.annotation.NonNull;

import com.erasmus.helper.ValueConverter;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

import nl.sense_os.input_kit.entity.IKValue;

/**
 * Created by panjiyudasetya on 7/21/17.
 */

public class Event<T> {
    private static final Gson GSON = new Gson();
    private static final String TOPIC = "topic";
    private static final String SAMPLES = "samples";
    private static final String EVENT_ID = "eventId";
    private static final String EVENT_NAME = "name";
    private String eventId;
    private String eventName;
    private String topic;
    private List<IKValue<T>> samples;
    private Callback completion;

    private Event(@NonNull String eventId,
                  @NonNull String eventName,
                  @NonNull String topic,
                  @NonNull List<IKValue<T>> samples,
                  @NonNull Callback completion) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.topic = topic;
        this.samples = samples;
        this.completion = completion;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getTopic() {
        return topic;
    }

    public List<IKValue<T>> getSamples() {
        return samples;
    }

    public Callback getCompletion() {
        return completion;
    }

    /**
     * Convert event into Android {@link Bundle}
     * @return {@link Bundle}
     */
    public String toJson() {
        JsonObject object = new JsonObject();
        object.addProperty(EVENT_ID, eventId);
        object.addProperty(EVENT_NAME, eventName);
        object.addProperty(TOPIC, topic);
        object.addProperty(SAMPLES, GSON.toJson(samples));
        return object.toString();
    }

    /**
     * Convert Event payload into writable map
     * @return {@link WritableMap}
     */
    public WritableMap toWritableMap() {
        WritableMap mapValue = Arguments.createMap();
        mapValue.putString(EVENT_ID, eventId);
        mapValue.putString(EVENT_NAME, eventName);
        mapValue.putString(TOPIC, topic);

        if (samples.isEmpty()) mapValue.putArray(SAMPLES, Arguments.createArray());
        else mapValue.putArray(SAMPLES, ValueConverter.toWritableArray(samples));
        return mapValue;
    }

    public static class Builder<T> {
        private String newEventId;
        private String newEventName;
        private String newTopic;
        private List<IKValue<T>> newSamples;
        private Callback newCompletion;

        public Builder eventId(@NonNull String newEventId) {
            this.newEventId = newEventId;
            return this;
        }

        public Builder eventName(@NonNull String newEventName) {
            this.newEventName = newEventName;
            return this;
        }

        public Builder topic(@NonNull String newTopic) {
            this.newTopic = newTopic;
            return this;
        }


        public Builder samples(@NonNull List<IKValue<T>> newSamples) {
            this.newSamples = newSamples;
            return this;
        }

        public Builder completion(@NonNull Callback newCompletion) {
            this.newCompletion = newCompletion;
            return this;
        }

        public Event build() {
            return new Event(
                    newEventId,
                    newEventName,
                    newTopic,
                    newSamples,
                    newCompletion
            );
        }
    }
}
