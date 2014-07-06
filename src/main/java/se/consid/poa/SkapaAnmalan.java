package se.consid.poa;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

/**
 * Klass för att skapa en anmälan.
 */
public class SkapaAnmalan extends Verticle {

	@Override
	public void start() {
		final Handler<Message<JsonObject>> skapaAnmalanHandler = new Handler<Message<JsonObject>>() {
			@Override
			public void handle(final Message<JsonObject> request) {
				container.logger().info(request.body());

				final JsonObject skapadAv = request.body()
						.getObject("skapadAv");

				final JsonObject anmalan = request.body().getObject("anmalan");
				
				final JsonObject update = createUpdate(skapadAv, anmalan);

				vertx.eventBus().send("test.mongodb", update,
						new Handler<Message<JsonObject>>() {
							@Override
							public void handle(
									final Message<JsonObject> dbResponse) {
								final JsonObject answer = dbResponse.body();
								container.logger().info(answer);

								request.reply(answer);

								anmalan.putString("id", answer.getString("_id"));


								fireEventAnmalanUppdaterad(anmalan);
							}
						});
			}

		};

		vertx.eventBus().registerHandler("skapa.anmalan", skapaAnmalanHandler,
				new Handler<AsyncResult<Void>>() {
					@Override
					public void handle(final AsyncResult<Void> result) {
						container.logger().info(
								"Skapa anmalan deploy " + result.succeeded());
					}
				});
	}

	private void fireEventAnmalanUppdaterad(final JsonObject event) {
		container.logger().info(
				"publishing anmalan.uppdaterad - type is created");
		vertx.eventBus().publish("anmalan.uppdaterad", event);
	}

	protected JsonObject createUpdate(final JsonObject skapadAv,
			final JsonObject anmalan) {

		anmalan.putArray("handelser",fabricateHandelse(skapadAv));

		final JsonObject upd = new JsonObject();

		final JsonObject update = new JsonObject();
		update.putString("action", "save");
		update.putString("collection", "anmalningar");
		update.putObject("document", anmalan);

		update.putObject("objNew", upd);

		return update;
	}

	private JsonArray fabricateHandelse(final JsonObject skapadAv) {
		JsonArray handelseArray = new JsonArray();

		JsonObject handelse = new JsonObject();
		handelse.putString("typ", "skapad");
		handelse.putString("tid", getTimeStamp());
		handelse.putObject("person", fabricatePerson(skapadAv));

		return handelseArray.add(handelse);
	}

	private JsonObject fabricatePerson(final JsonObject skapadAv) {
		JsonObject person = new JsonObject();
		person.putString("firstname", skapadAv.getString("firstname"));
		person.putString("lastname", skapadAv.getString("lastname"));
		person.putString("email", skapadAv.getString("epost"));
		return person;
	}

	private String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime().toString();
	}

}
