package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request.application.CreateShipmentRequestImpl;
import request.application.ValidateShipmentRequestImpl;
import request.infrastructure.ShipmentRequestEventProducer;
import request.infrastructure.ShipmentRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").load(); //carica le variabili del file .env
        String bootstrap = dotenv.get("KAFKA_BOOTSTRAP_SERVERS"); //legge il campo

        int port = Integer.parseInt(dotenv.get("PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i use case
        CreateShipmentRequestImpl createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequestImpl validateShipmentRequest = new ValidateShipmentRequestImpl();

        //crea il producer Kafka
        ShipmentRequestEventProducer eventProducer = new ShipmentRequestEventProducer(vertx, bootstrap);

        //crea il controller REST
        ShipmentRequestController controller = new ShipmentRequestController(createShipmentRequest, validateShipmentRequest, eventProducer);

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        controller.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}