import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import verticles.ApiServer;
import verticles.EventHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Application {
    private static EventBus eventBus;
    private static final Logger logger = LogManager.getLogger(Application.class.getName());


    public static void main(String[] args) throws IOException {
        if(args.length!=1){
            logger.error("There is no valid path in the input argument, the input argument need contain only one process path");
            return;
        }

        Vertx vertx = Vertx.vertx();
        eventBus = vertx.eventBus();

        vertx.deployVerticle(new EventHandler());
        vertx.deployVerticle(new ApiServer());

        String cmd = args[0];
        BufferedReader input;

        logger.info("Start listening to process: {}",cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        input = new BufferedReader(new InputStreamReader(p.getInputStream()));
        input.lines().forEach(line -> {
            if(logger.isDebugEnabled()){
                logger.debug("received event: {}",line);
            }
            eventBus.send(EventHandler.class.getName(), line);
        });

        logger.info("close input reader buffer from process");
        input.close();
    }
}
