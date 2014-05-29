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

        final String fromEmail = container.config().getString("fromEmail");
        final String addressMailMod = container.config().getString("address");

        final Handler<Message<JsonObject>> skickaTillPolisenHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> request) {
                container.logger().info("mu");
                container.logger().info(request.body());
                final JsonObject body = request.body();

                container.logger().info("Skickar mail till polisen på adress: " + fromEmail);
                final JsonObject update = createMail(fromEmail, body);

                vertx.eventBus().send(addressMailMod, update, new Handler<Message<JsonObject>>() {
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

        vertx.eventBus().registerHandler("skicka.till.polisen",
                skickaTillPolisenHandler, new Handler<AsyncResult<Void>>() {
                    @Override
                    public void handle(final AsyncResult<Void> result) {
                        container.logger().info("Skicka till polisen deploy " + result.succeeded());
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

    private JsonObject createMail(final String fromEmail, final JsonObject body) {
        final JsonObject mail = new JsonObject();
        mail.putString("from", fromEmail);
        mail.putString("to", fromEmail); //TODO: rätt e-postadress
        mail.putString("subject", "Anmälan till polisen på " + body.getString("subject"));
        mail.putString("body", String.format("Hej Polisen, detta är en anmälan. %s Från SB", body.getString("body")));

        return mail;
    }

}
