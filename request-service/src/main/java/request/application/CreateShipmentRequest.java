package request.application;

import buildingblocks.application.InboundPort;
import request.domain.Package;
import request.domain.User;
import request.domain.Position;
import request.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;

@InboundPort
public interface CreateShipmentRequest {
    Shipment create(String userId, String userName, String userSurname, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, String pickupDate, String pickupTime, Integer timeLimit, Double weight, Boolean fragile);
}