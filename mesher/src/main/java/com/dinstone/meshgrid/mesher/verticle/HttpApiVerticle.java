
package com.dinstone.meshgrid.mesher.verticle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dinstone.meshgrid.mesher.handler.AccessLogHandler;
import com.dinstone.meshgrid.mesher.handler.ServiceHandler;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpApiVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(HttpApiVerticle.class);

    private JsonObject configuration;

    private ServiceHandler serviceHandler;

    public HttpApiVerticle(JsonObject configuration, ServiceHandler serviceHandler) {
        super();
        this.configuration = configuration;
        this.serviceHandler = serviceHandler;
    }

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);

        // configuration = config();

        // String registryName = config().getString("app.metrics.registry", "app.metrics");
        // metricRegistry = SharedMetricRegistries.getOrCreate(registryName);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        Router mainRouter = Router.router(vertx);
        mainRouter.route().failureHandler(rc -> {
            LOG.error("handler logic occur error", rc.failure());
            rc.response().end();
        });
        mainRouter.route().handler(new AccessLogHandler());
        mainRouter.route().handler(BodyHandler.create());

        mainRouter.route("/*").handler(serviceHandler::api);

        // http server
        String type = config().getString("type");
        int port = config().getInteger("port", 8080);
        String host = config().getString("host", "0.0.0.0");
        HttpServerOptions serverOptions = new HttpServerOptions().setIdleTimeout(180);
        vertx.createHttpServer(serverOptions).requestHandler(mainRouter::accept).listen(port, host, ar -> {
            if (ar.succeeded()) {
                LOG.info("start http success, {} on {}:{}", type, host, port);
                startFuture.complete();
            } else {
                LOG.error("start http failed, {} on {}:{}", type, host, port);
                startFuture.fail(ar.cause());
            }
        });
    }

}