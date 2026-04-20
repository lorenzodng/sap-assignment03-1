package delivery;

import delivery.application.ShipmentManager;
import delivery.application.ShipmentManagerImpl;
import delivery.application.ShipmentRepository;
import delivery.infrastructure.InMemoryShipmentRepository;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import delivery.infrastructure.DroneAssignedEventConsumer;
import delivery.infrastructure.DroneUnavailableEventConsumer;
import delivery.infrastructure.TrackingDeliveryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").ignoreIfMissing().load();
        String bootstrap = System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null ? System.getenv("KAFKA_BOOTSTRAP_SERVERS") : dotenv.get("KAFKA_BOOTSTRAP_SERVERS");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));

        Vertx vertx = Vertx.vertx();

        ShipmentRepository repository = new InMemoryShipmentRepository();

        ShipmentManager shipmentManager = new ShipmentManagerImpl(repository);

        new DroneAssignedEventConsumer(vertx, bootstrap, shipmentManager);
        new DroneUnavailableEventConsumer(vertx, bootstrap, shipmentManager);

        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipmentManager);

        Router router = Router.router(vertx);
        trackingController.registerRoutes(router);

        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}