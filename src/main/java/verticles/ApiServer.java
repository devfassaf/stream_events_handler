package verticles;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.core.shareddata.SharedData;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ApiServer extends AbstractVerticle {
    private final Logger logger = LogManager.getLogger(this.getClass().getName());
    private LocalMap<String, Long> eventTypeBucket;
    private LocalMap<String, Long> dataBucket;
    private LocalMap<String, Long> invalidBucket;

    @Override
    public void start() {
        logger.info("ApiServer start");
        SharedData sharedData = vertx.sharedData();
        this.eventTypeBucket = sharedData.getLocalMap(SharedObjectName.EVENT_TYPE_BUCKET_NAME);
        this.dataBucket = sharedData.getLocalMap(SharedObjectName.DATA_BUCKET_NAME);
        this.invalidBucket = sharedData.getLocalMap(SharedObjectName.INVALID_BUCKET_NAME);


        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/ping").handler(this::ping);
        router.get("/event_type/:type").handler(this::handleEventTypeBucketSize);
        router.get("/data/:data").handler(this::handleGetDataBucketSize);
        router.get("/event_type").handler(this::handleEventTypeList);
        router.get("/data").handler(this::handleDataList);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void ping(RoutingContext routingContext) {
        logger.info("Received ping request");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        routingContext.response().putHeader("content-type", "text/html")
                .end(String.format("pong :-) %s", dateFormat.format(date)));
    }

    private void handleEventTypeBucketSize(RoutingContext routingContext) {
        logger.info("Received bucket event_type size request");
        responseBucketSize(routingContext, "type", eventTypeBucket);
    }

    private void handleGetDataBucketSize(RoutingContext routingContext) {
        logger.info("Received bucket data size request");
        responseBucketSize(routingContext, "data", dataBucket);
    }

    private void handleEventTypeList(RoutingContext routingContext) {
        logger.info("Received get all event_types request");
        responseBucketKeys(routingContext, eventTypeBucket);
    }

    private void handleDataList(RoutingContext routingContext) {
        logger.info("Received get all data request");
        responseBucketKeys(routingContext, dataBucket);
    }

    private void responseBucketKeys(RoutingContext routingContext, LocalMap<String, Long> bucket) {
        JsonArray arr = new JsonArray();
        bucket.keySet().stream().forEach(k -> arr.add(k));
        routingContext.response().putHeader("content-type", "application/json").end(arr.encodePrettily());
    }

    private void responseBucketSize(RoutingContext routingContext, String param, LocalMap<String, Long> bucket) {
        String type = routingContext.request().getParam(param);
        HttpServerResponse response = routingContext.response();
        if (type == null) {
            sendError(400, response);
        } else {
            Long counter = bucket.get(type);
            if (counter == null) {
                sendError(404, response);
            } else {
                response.putHeader("content-type", "application/json").end(counter.toString());
            }
        }
    }

    private void sendError(int statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode).end();
    }

    @Override
    public void stop() {
        logger.info("ApiServer stop");
    }
}
