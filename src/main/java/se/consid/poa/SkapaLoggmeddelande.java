package se.consid.poa;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Klass för att skapa loggmeddelanden för en Anmalan.
 */
public class SkapaLoggmeddelande extends Verticle {
    @Override
    public void start() {
        final Handler<Message<JsonObject>> skapaLoggmeddelandeHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> request) {
                container.logger().info(request.body());
                final JsonObject body = request.body();

                final JsonObject update = createUpdate(body);

                vertx.eventBus().send("test.mongodb", update, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(final Message<JsonObject> dbResponse) {
                    final JsonObject answer = dbResponse.body();
                    container.logger().info(answer);

                    request.reply(answer);

                    fireEventAnmalanUppdaterad(createUpdateEvent(body));
                    }
                });
            }
        };

        vertx.eventBus().registerHandler("skapa.loggmeddelande",
                skapaLoggmeddelandeHandler, new Handler<AsyncResult<Void>>() {
            @Override
            public void handle(final AsyncResult<Void> result) {
                container.logger().info("SkapaLog deploy " + result.succeeded());
            }
        });
    }

    private JsonObject createUpdateEvent(final JsonObject request) {
        final JsonObject event = new JsonObject();
        event.putString("id", request.getInteger("id").toString());
        event.putString("username", request.getString("username"));
        event.putString("subject", "Unknown"); // TODO lägg till titel på Anmalan.
        return event;
    }

    private void fireEventAnmalanUppdaterad(final JsonObject event) {
        container.logger().info("publishing anmalan.uppdaterad");
        vertx.eventBus().publish("anmalan.uppdaterad", event);
    }

    protected JsonObject createUpdate(final JsonObject request) {
        final int id = request.getInteger("id");

        final JsonObject upd = new JsonObject();
        upd.putObject("$push", new JsonObject().putObject("loggar", request));

        final JsonObject update = new JsonObject();
        update.putString("action", "update");
        update.putString("collection", "anmalningar");
        update.putObject("criteria", new JsonObject().putNumber("_id", id));
        update.putObject("objNew", upd);

        return  update;
    }
}
