package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.json.JSONObject;
import request.application.ShipmentRequestEventProducer;
import request.domain.Shipment;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Adapter
public class KafkaShipmentRequestEventProducer implements ShipmentRequestEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaShipmentRequestEventProducer.class);
    private static final String TOPIC = "shipment-requested";
    private final KafkaProducer<String, String> producer;

    public KafkaShipmentRequestEventProducer(Vertx vertx, String bootstrapServers) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = KafkaProducer.create(vertx, config);
    }

    @Override
    public Future<Void> publishShipmentRequested(Shipment shipment) {

        JSONObject event = new JSONObject();
        event.put("shipmentId", shipment.getId());
        event.put("pickupLatitude", shipment.getPickupLocation().getLatitude());
        event.put("pickupLongitude", shipment.getPickupLocation().getLongitude());
        event.put("deliveryLatitude", shipment.getDeliveryLocation().getLatitude());
        event.put("deliveryLongitude", shipment.getDeliveryLocation().getLongitude());
        event.put("packageWeight", shipment.getPackage().getWeight());
        event.put("deliveryTimeLimit", shipment.getDeliveryTimeLimit());

        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(TOPIC, shipment.getId(), event.toString());
        return producer.send(record)
                .onSuccess(v -> log.info("Shipment {} request event published", shipment.getId()))
                .onFailure(err -> log.error("Failed to publish event for shipment {}", shipment.getId(), err))
                .mapEmpty();
    }
}