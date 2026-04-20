package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request.application.*;
import request.infrastructure.KafkaShipmentRequestEventProducer;
import request.infrastructure.PrometheusRequestMetricsProxy;
import request.infrastructure.ShipmentRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").ignoreIfMissing().load();
        String bootstrap = System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null ? System.getenv("KAFKA_BOOTSTRAP_SERVERS") : dotenv.get("KAFKA_BOOTSTRAP_SERVERS");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        Vertx vertx = Vertx.vertx();

        ShipmentRequestEventProducer eventProducer = new KafkaShipmentRequestEventProducer(vertx, bootstrap);

        CreateShipmentRequestImpl createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequestImpl validateShipmentRequest = new ValidateShipmentRequestImpl();
        ShipmentScheduler shipmentScheduler = new ShipmentSchedulerImpl(eventProducer, vertx);

        RequestMetrics metrics = null;
        try {
            metrics = new PrometheusRequestMetricsProxy(metricsPort);
            log.info("Prometheus metrics exporter started on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics exporter: {}", e.getMessage());
        }

        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(createShipmentRequest, validateShipmentRequest, shipmentScheduler, metrics);

        ShipmentRequestController controller = new ShipmentRequestController(orchestrator);

        Router router = Router.router(vertx);
        controller.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}