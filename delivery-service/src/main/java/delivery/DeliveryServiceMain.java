package delivery;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import delivery.domain.Shipment;
import delivery.infrastructure.DroneAssignedEventConsumer;
import delivery.infrastructure.DroneUnavailableEventConsumer;
import delivery.infrastructure.TrackingDeliveryController;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").load(); //carica le variabili del file .env
        String bootstrap = dotenv.get("KAFKA_BOOTSTRAP_SERVERS"); //legge il campo

        int port = Integer.parseInt(dotenv.get("PORT"));

        Vertx vertx = Vertx.vertx();

        //crea i consumer Kafka
        Map<String, Shipment> shipments = new HashMap<>();
        new DroneAssignedEventConsumer(vertx, bootstrap, shipments);
        new DroneUnavailableEventConsumer(vertx, bootstrap, shipments);

        //crea il controller REST
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipments);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        trackingController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}