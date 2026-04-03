package drone;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import drone.application.AssignDroneImpl;
import drone.application.CheckDroneAvailabilityImpl;
import drone.domain.Drone;
import drone.domain.Position;
import drone.infrastructure.DroneEventProducer;
import drone.infrastructure.ShipmentRequestedEventConsumer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DroneServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("drone-service").load(); //carica le variabili del file .env
        String bootstrap = dotenv.get("KAFKA_BOOTSTRAP_SERVERS"); //legge il campo

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea i use case
        CheckDroneAvailabilityImpl checkDroneAvailability = new CheckDroneAvailabilityImpl();
        AssignDroneImpl assignDrone = new AssignDroneImpl(checkDroneAvailability);

        //crea la flotta di droni (posizionati a Roma)
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("drone-1", new Position(41.90, 12.49)));
        drones.add(new Drone("drone-2", new Position(41.91, 12.50)));
        drones.add(new Drone("drone-3", new Position(41.92, 12.51)));

        //crea il producer Kafka
        DroneEventProducer eventProducer = new DroneEventProducer(vertx, bootstrap);

        //crea il consumer Kafka
        new ShipmentRequestedEventConsumer(vertx, bootstrap, assignDrone, drones, eventProducer);

        log.info("Drone service started");
    }
}