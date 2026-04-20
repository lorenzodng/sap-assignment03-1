package gateway;

import gateway.application.ApiGatewayMetrics;
import gateway.infrastructure.PrometheusApiGatewayMetricsProxy;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import gateway.infrastructure.ApiGatewayController;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiGatewayMain {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("api-gateway").ignoreIfMissing().load();
        String requestServiceUrl = System.getenv("REQUEST_SERVICE_URL") != null ? System.getenv("REQUEST_SERVICE_URL") : dotenv.get("REQUEST_SERVICE_URL");
        String deliveryServiceUrl = System.getenv("DELIVERY_SERVICE_URL") != null ? System.getenv("DELIVERY_SERVICE_URL") : dotenv.get("DELIVERY_SERVICE_URL");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        Vertx vertx = Vertx.vertx();

        ApiGatewayMetrics metrics = null;
        try {
            metrics = new PrometheusApiGatewayMetricsProxy(metricsPort);
            log.info("Prometheus metrics available on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics server: {}", e.getMessage());
        }

        ApiGatewayController apiGatewayController = new ApiGatewayController(vertx, requestServiceUrl, deliveryServiceUrl, metrics);

        Router router = Router.router(vertx);
        apiGatewayController.registerRoutes(router);

        router.route("/ui/*").handler(StaticHandler.create("webroot"));

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Api gateway started on port {}", port);
    }
}