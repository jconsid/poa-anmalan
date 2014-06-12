package se.consid.poa;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.Date;

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

                        addEvent(body);
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

    protected JsonObject createUpdate(final JsonObject request) {
        final String anmalanId = request.getString("id");

        final String tid = new Date().toString();

        final JsonObject person = request.getObject("skapadAv");

        final JsonObject handelse = new JsonObject();
        handelse.putString("typ", "skickad");
        handelse.putString("tid", tid);
        handelse.putObject("person", person);

        final JsonObject upd = new JsonObject();
        upd.putObject("$push", new JsonObject().putObject("handelser", handelse));
        upd.putObject("$set", new JsonObject().putString("anmalningsstatus", "SKICKAD"));

        final JsonObject update = new JsonObject();
        update.putString("action", "update");
        update.putString("collection", "anmalningar");
        update.putObject("criteria", new JsonObject().putString("_id", anmalanId));
        update.putObject("objNew", upd);

        return  update;
    }

    private void addEvent(final JsonObject body){

        vertx.eventBus().send("test.mongodb", createUpdate(body), new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> vertxResponse) {
                final JsonObject answer = vertxResponse.body();
                container.logger().info(answer);

                fireEventAnmalanUppdaterad(createUpdateEvent(body));
            }
        });
    }

    private JsonObject createUpdateEvent(final JsonObject request) {
        final JsonObject event = new JsonObject();
        event.putString("id", request.getString("id"));
        event.putObject("skapadAv", request.getObject("skapadAv"));
        event.putString("title", request.getString("title"));
        return event;
    }

    private void fireEventAnmalanUppdaterad(final JsonObject event) {
        container.logger().info("publishing anmalan.uppdaterad");
        vertx.eventBus().publish("anmalan.uppdaterad", event);
    }

}
