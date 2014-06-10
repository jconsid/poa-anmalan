package se.consid.poa;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Created by JOHA on 2014-06-10.
 */
public class SkickaTillPolisenController extends Verticle {

    @Override
    public void start() {

        final Handler<Message<JsonObject>> skickaTillPolisenHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> request) {
                container.logger().info(request.body());
                final JsonObject body = request.body();

                vertx.eventBus().send("skicka.till.polisen.epost", body, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(final Message<JsonObject> vertxResponse) {
                        final JsonObject answer = vertxResponse.body();
                        container.logger().info(answer);

                        request.reply(answer);

                        fireEventAnmalanUppdaterad(createUpdateEvent(body));
                    }
                });

            }
        };

        vertx.eventBus().registerHandler("skicka.till.polisen",
                skickaTillPolisenHandler, new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(final AsyncResult<Void> result) {
                        container.logger().info("Skicka till polisen Controller deploy " + result.succeeded());
                    }
                }
        );

    }

    private JsonObject createUpdateEvent(final JsonObject request) {
        final JsonObject event = new JsonObject();
        event.putString("id", request.getInteger("id").toString());
        event.putString("username", request.getString("username"));
        event.putString("subject", "Unknown"); // TODO lägg till titel pa Anmälan.
        return event;
    }

    private void fireEventAnmalanUppdaterad(final JsonObject event) {
        container.logger().info("publishing anmalan.uppdaterad");
        vertx.eventBus().publish("anmalan.uppdaterad", event);
    }

}
