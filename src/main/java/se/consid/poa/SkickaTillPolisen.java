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
        container.logger().info("SkickaTillPolisen : fromEmail: " + fromEmail);
        container.logger().info("SkickaTillPolisen : addressMailMod: " + addressMailMod);

        final Handler<Message<JsonObject>> skickaTillPolisenHandler = new Handler<Message<JsonObject>>() {
            @Override
            public void handle(final Message<JsonObject> request) {
                container.logger().info(request.body());
                final JsonObject body = request.body();

                final JsonObject update = createMail(fromEmail, body);

                vertx.eventBus().send(addressMailMod, update, new Handler<Message<JsonObject>>() {
                    @Override
                    public void handle(final Message<JsonObject> mailResponse) {
                        final JsonObject answer = mailResponse.body();
                        container.logger().info(answer);

                        request.reply(answer);

                        fireEventEpostSkickad(createUpdateEvent(body));
                    }
                });

            }
        };

         vertx.eventBus().registerHandler("skicka.till.polisen.epost",
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
        event.putString("subject", "Unknown"); // TODO lägg till titel pa Anmälan.
        return event;
    }

    private void fireEventEpostSkickad(final JsonObject event) {
        container.logger().info("anmalan skickad till polis");
        vertx.eventBus().publish("anmalan.skickad", event);
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
