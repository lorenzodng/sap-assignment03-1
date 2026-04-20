package delivery.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery.application.ShipmentManager;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Adapter
public class DroneAssignedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DroneAssignedEventConsumer.class);
    private static final String TOPIC = "drone-assigned";
    private final KafkaConsumer<String, String> consumer;
    private final ShipmentManager shipmentManager;

    public DroneAssignedEventConsumer(Vertx vertx, String bootstrapServers, ShipmentManager shipmentManager) {
        this.shipmentManager = shipmentManager;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "delivery-management-group");
        config.put("auto.offset.reset", "earliest");
        this.consumer = KafkaConsumer.create(vertx, config);
        this.consumer.subscribe(TOPIC);
        this.consumer.handler(record -> scheduleShipment(record.value()));
    }

    private void scheduleShipment(String message) {
        String shipmentId = "unknown";
        try {
            JSONObject event = new JSONObject(message);
            shipmentId = event.getString("shipmentId");
            log.info("Shipment {} drone assigned event received", shipmentId);
            shipmentManager.createShipmentFromAssignment(shipmentId, true, event.getDouble("droneLatitude"), event.getDouble("droneLongitude"), event.getDouble("pickupLatitude"), event.getDouble("pickupLongitude"), event.getDouble("deliveryLatitude"), event.getDouble("deliveryLongitude"), event.getLong("assignedAt"), event.getDouble("droneSpeed"));
        } catch (Exception ex) {
            log.info("Failed to receive shipment {} assignment event", shipmentId, ex);
        }
    }
}