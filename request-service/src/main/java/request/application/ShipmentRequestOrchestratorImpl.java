package request.application;

import io.vertx.core.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import request.domain.Shipment;

public class ShipmentRequestOrchestratorImpl implements ShipmentRequestOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(ShipmentRequestOrchestratorImpl.class);
    private final CreateShipmentRequest creator;
    private final ValidateShipmentRequest validator;
    private final ShipmentScheduler scheduler;
    private final RequestMetrics metrics;

    public ShipmentRequestOrchestratorImpl(CreateShipmentRequest creator, ValidateShipmentRequest validator, ShipmentScheduler scheduler, RequestMetrics metrics) {
        this.creator = creator;
        this.validator = validator;
        this.scheduler = scheduler;
        this.metrics = metrics;
    }

    @Override
    public Future<Shipment> orchestrateRequest(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer deliveryTimeLimit, Double weight, Boolean fragile) {
        long startTime = System.currentTimeMillis();
        try {
            //step 1: create and validate the request
            Shipment shipment = creator.create(userId, userName, userSurname, pickupLat, pickupLon, deliveryLat, deliveryLon, pickupDate, pickupTime, deliveryTimeLimit, weight, fragile);
            validator.validate(shipment);
            log.info("Shipment {} request created", shipment.getId());

            //step 2: validate timing constraints and publish the event
            return scheduler.schedule(shipment).map(v -> shipment);
        } catch (Exception e) {
            return Future.failedFuture(e);
        } finally {
            long endTime = System.currentTimeMillis();
            double durationSeconds = (endTime - startTime) / 1000.0;
            metrics.observeLatency(durationSeconds);
        }
    }
}