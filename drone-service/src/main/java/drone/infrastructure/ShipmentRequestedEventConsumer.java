package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneAssignmentOrchestrator;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//recupera l'evento di creazione richiesta spedizione pubblicato dal gestore richieste
@Adapter
public class ShipmentRequestedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ShipmentRequestedEventConsumer.class);
    private static final String TOPIC = "shipment-requested";
    private final KafkaConsumer<String, String> consumer;
    private final DroneAssignmentOrchestrator orchestrator;

    public ShipmentRequestedEventConsumer(Vertx vertx, String bootstrapServers, DroneAssignmentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "drone-management-group"); //gruppo di consumer Kafka usato per tenere traccia di quali messaggi sono già stati letti da un gruppo di istanze di uno stesso microservizio - in questo modo kafka distribuisce i messaggi tra loro senza duplicati
        config.put("auto.offset.reset", "earliest"); //legge tutti i messaggi dall'inizio del topic, anche quelli pubblicati prima dell'esistenza del consumer
        this.consumer = KafkaConsumer.create(vertx, config);
        this.consumer.subscribe(TOPIC); //sottoscrive il consumatore al topic di richieste
        this.consumer.handler(record -> assignDroneToShipment(record.value())); //registra il metodo di gestioine delle richieste
    }

    //assegna un drone alla spedizione e invoca il broker kafka
    private void assignDroneToShipment(String message) {
        String shipmentId = "unknown";
        try {
            JSONObject event = new JSONObject(message);
            shipmentId = event.getString("shipmentId");
            log.info("Shipment {} request event received", shipmentId);
            orchestrator.handleShipmentRequested(shipmentId, event.getDouble("pickupLatitude"), event.getDouble("pickupLongitude"), event.getDouble("deliveryLatitude"), event.getDouble("deliveryLongitude"), event.getDouble("packageWeight"), event.getInt("deliveryTimeLimit"));
        } catch (Exception ex) {
            log.info("Failed to receive shipment {} request event", shipmentId, ex);
        }
    }
}