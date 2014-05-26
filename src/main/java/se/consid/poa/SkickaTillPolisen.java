package se.consid.poa;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Klass för att skicka anmälan till polisen.
 */
public class SkickaTillPolisen extends Verticle {

    @Override
    public void start() {

        final String email = container.config().getString("email");

        final Handler<Message<JsonObject>> skickaTillPolisenHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> request) {
                container.logger().info(request.body());
                final JsonObject body = request.body();

                container.logger().info("Skickar mail till polisen på adress: " + email);

                //https://github.com/vert-x/mod-mailer
            }
        };

        vertx.eventBus().registerHandler("skicka.till.polisen",
                skickaTillPolisenHandler, new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(final AsyncResult<Void> result) {
                        container.logger().info("Skicka till polisen deploy " + result.succeeded());
                    }
                }
        );
    }
}
