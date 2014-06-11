package se.consid.poa;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Date;

/**
 * Klass för att skapa loggmeddelanden för en Anmalan.
 */
public class SkapaLoggmeddelande extends Verticle {
    @Override
    public void start() {
        final Handler<Message<JsonObject>> skapaLoggmeddelandeHandler = new Handler<Message<JsonObject>>() {
            public void handle(final Message<JsonObject> request) {
                container.logger().info(request.body());
                final JsonObject body = request.body();

                final JsonObject update = createUpdate(body);

                vertx.eventBus().send("test.mongodb", update, new Handler<Message<JsonObject>>() {
                    public void handle(final Message<JsonObject> dbResponse) {
                        final JsonObject answer = dbResponse.body();
                        container.logger().info(answer);

                        request.reply(answer);

                        fireUpdateEvent(body);
                    }
                });
            }
        };

        vertx.eventBus().registerHandler("skapa.loggmeddelande",
                skapaLoggmeddelandeHandler, new Handler<AsyncResult<Void>>() {
                    public void handle(final AsyncResult<Void> result) {
                        container.logger().info("SkapaLog deploy " + result.succeeded());
                    }
                }
        );
    }

    private void fireUpdateEvent(final JsonObject request) {
        final JsonObject query = new JsonObject();
        query.putString("action", "find");
        query.putString("collection", "anmalningar");
        query.putObject("matcher", new JsonObject().putString("_id", request.getString("id")));

        vertx.eventBus().send("test.mongodb", query, new Handler<Message<JsonObject>>() {
            public void handle(final Message<JsonObject> dbResponse) {
                final JsonObject anmalan = dbResponse.body().getArray("results").get(0);

                container.logger().info("publishing anmalan.uppdaterad");
                vertx.eventBus().publish("anmalan.uppdaterad", anmalan);
            }
        });
    }

    protected JsonObject createUpdate(final JsonObject request) {
        final String anmalanId = request.getString("id");

        final String tid = new Date().toString();

        final JsonObject person = new JsonObject();
        person.putString("firstname", "(Bosse)");

        final JsonObject logEntry = new JsonObject();
        logEntry.putString("rubrik", request.getString("subject"));
        logEntry.putString("meddelande", request.getString("body"));
        logEntry.putString("tid", tid);
        logEntry.putObject("person", person);

        final JsonObject handelse = new JsonObject();
        handelse.putString("typ", "logg");
        handelse.putString("tid", tid);
        handelse.putObject("person", person);

        final JsonObject upd = new JsonObject();
        upd.putObject("$push", new JsonObject().putObject("loggbok", logEntry).putObject("handelser", handelse));

        final JsonObject update = new JsonObject();
        update.putString("action", "update");
        update.putString("collection", "anmalningar");
        update.putObject("criteria", new JsonObject().putString("_id", anmalanId));
        update.putObject("objNew", upd);

        return update;
    }

}
