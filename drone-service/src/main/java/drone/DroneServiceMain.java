package drone;

import drone.application.*;
import drone.infrastructure.InMemoryDroneRepository;
import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import drone.domain.Drone;
import drone.domain.Position;
import drone.infrastructure.KafkaDroneEventProducer;
import drone.infrastructure.ShipmentRequestedEventConsumer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroneServiceMain {

    private static final Logger log = LoggerFactory.getLogger(DroneServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("drone-service").ignoreIfMissing().load();
        String bootstrap = System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null ? System.getenv("KAFKA_BOOTSTRAP_SERVERS") : dotenv.get("KAFKA_BOOTSTRAP_SERVERS");

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //crea la flotta di droni (posizionati a Roma)
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("drone-1", new Position(41.90, 12.49)));
        drones.add(new Drone("drone-2", new Position(41.91, 12.50)));
        drones.add(new Drone("drone-3", new Position(41.92, 12.51)));

        //crea il livello infrastruttura
        DroneRepository droneRepository = new InMemoryDroneRepository(drones);
        DroneEventProducer eventProducer = new KafkaDroneEventProducer(vertx, bootstrap);

        //crea i use case
        CheckDroneAvailabilityImpl checkDroneAvailability = new CheckDroneAvailabilityImpl();
        AssignDroneImpl assignDrone = new AssignDroneImpl(checkDroneAvailability);

        //crea l'orchestratore
        DroneAssignmentOrchestrator orchestrator = new DroneAssignmentOrchestratorImpl(assignDrone, eventProducer, droneRepository);

        //crea il consumer Kafka
        new ShipmentRequestedEventConsumer(vertx, bootstrap, orchestrator);

        log.info("Drone service started");
    }
}