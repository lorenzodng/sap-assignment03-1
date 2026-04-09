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
        Dotenv dotenv = Dotenv.configure().directory("delivery-service").load(); //carica le variabili del file .env
        String bootstrap = dotenv.get("KAFKA_BOOTSTRAP_SERVERS"); //legge il campo
        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea il repository
        ShipmentRepository repository = new InMemoryShipmentRepository();

        //crea il manager
        ShipmentManager shipmentManager = new ShipmentManagerImpl(repository);

        //crea i consumer Kafka
        new DroneAssignedEventConsumer(vertx, bootstrap, shipmentManager);
        new DroneUnavailableEventConsumer(vertx, bootstrap, shipmentManager);

        //crea il controller
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipmentManager);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        trackingController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Delivery service started on port {}", port);
    }
}