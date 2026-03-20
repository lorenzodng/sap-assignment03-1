package request_management.application;

import buildingblocks.application.InboundPort;
import request_management.domain.Package;
import request_management.domain.Position;
import request_management.domain.Shipment;

import java.time.LocalDateTime;

@InboundPort
public interface CreateShipmentRequest {
    Shipment create(String id, Position pickupLocation, Position deliveryLocation, LocalDateTime pickupTime, int deliveryTimeLimit, Package pack);
}