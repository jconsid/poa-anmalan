package se.consid.poa;

import org.junit.Assert;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;
import static org.vertx.testtools.VertxAssert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by JOHA on 2014-05-29.
 */
public class SkickaTillPolisenModuleIntegrationTest extends TestVerticle {

    @Test
    public void testPing() {
        container.logger().info("in testPing()");
        vertx.eventBus().send("ping-address", "ping!", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> reply) {
                assertEquals("pong!", reply.body());


                // If we get here, the test is complete
                // You must always call `testComplete()` at the end. Remember that testing is *asynchronous* so
                // we cannot assume the test is complete by the time the test method has finished executing like
                // in standard synchronous tests

                testComplete();
            }
        });
    }

    @Override
    public void start() {
        initialize();

        container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                // Deployment is asynchronous and this this handler will be called when it's complete (or failed)
                VertxAssert.assertTrue(asyncResult.succeeded());
                Assert.assertNotNull("deploymentID should not be null", asyncResult.result());
                // If deployed correctly then start the tests!
                startTests();
            }
        });
    }
}
