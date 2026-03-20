package request_management.domain;

import buildingblocks.domain.AggregateRoot;

import java.time.LocalDateTime;

public class Shipment implements AggregateRoot<String> {

    //final in modo che la richiesta non possa essere modificata una volta creata
    private final String id;
    private final Position pickupLocation;
    private final Position deliveryLocation;
    private final LocalDateTime pickupTime;
    private final int deliveryTimeLimit;
    private final Package pack;
    private ShipmentStatus status;

    public Shipment(String id, Position pickupLocation, Position deliveryLocation, LocalDateTime pickupTime, int deliveryTimeLimit, Package pack) {
        this.id = id;
        this.pickupLocation = pickupLocation;
        this.deliveryLocation = deliveryLocation;
        this.pickupTime = pickupTime;
        this.deliveryTimeLimit = deliveryTimeLimit;
        this.pack = pack;
        this.status = ShipmentStatus.REQUESTED;
    }

    @Override
    public String getId() {
        return id;
    }

    public Position getPickupLocation() {
        return pickupLocation;
    }

    public Position getDeliveryLocation() {
        return deliveryLocation;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public int getDeliveryTimeLimit() {
        return deliveryTimeLimit;
    }

    public Package getPackage() {
        return pack;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public void updateStatus(ShipmentStatus newStatus) {
        this.status = newStatus;
    }
}