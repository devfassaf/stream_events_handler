package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import module.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static verticles.SharedObjectName.*;

public class EventHandler extends AbstractVerticle {
    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private LocalMap<String, Long> eventTypeBucket;
    private LocalMap<String, Long> dataBucket;
    private LocalMap<String, Long> invalidBucket;

    @Override
    public void start() {
        logger.info("EventHandler start");
        EventBus eventBus = vertx.eventBus();
        SharedData sharedData = vertx.sharedData();
        this.eventTypeBucket = sharedData.getLocalMap(EVENT_TYPE_BUCKET_NAME);
        this.dataBucket = sharedData.getLocalMap(DATA_BUCKET_NAME);
        this.invalidBucket = sharedData.getLocalMap(INVALID_BUCKET_NAME);

        MessageConsumer<String> consumer = eventBus.consumer(this.getClass().getName());
        consumer.handler(message -> parseEvent(message.body()));
    }

    private void parseEvent(String message) {
        JsonObject jsonObject;
        try {
            jsonObject = new JsonObject(message);
        } catch (DecodeException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received invalid event: {}", message);
            }
            addToBucket(message, invalidBucket);
            return;
        }
        Event event = Json.mapper.convertValue(jsonObject.getMap(), Event.class);
        addToBucket(event.getEvent_type(), eventTypeBucket);
        String[] words = event.getData().split(" ");
        for (int i = 0; i < words.length; i++) {
            addToBucket(words[i], dataBucket);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("EventTypeBucket size= {}, DataBucket size= {}, InvalidBucket size={}, Event= {}", eventTypeBucket.size(), dataBucket.size(), invalidBucket.size(), message);
        }
    }

    private void addToBucket(String key, LocalMap<String, Long> bucket) {
        synchronized (bucket) {
            bucket.put(key, bucket.getOrDefault(key, 0L) + 1);
        }
    }

    @Override
    public void stop() {
        logger.info("EventHandler stop");
    }
}
