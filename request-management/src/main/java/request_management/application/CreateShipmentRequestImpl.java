package request_management.application;

import request_management.domain.Package;
import request_management.domain.Position;
import request_management.domain.Shipment;
import java.time.LocalDateTime;

public class CreateShipmentRequestImpl implements CreateShipmentRequest {

    @Override
    public Shipment create(String id, Position pickupLocation, Position deliveryLocation, LocalDateTime pickupTime, int deliveryTimeLimit, Package pack) {
        return new Shipment(id, pickupLocation, deliveryLocation, pickupTime, deliveryTimeLimit, pack);
    }
}