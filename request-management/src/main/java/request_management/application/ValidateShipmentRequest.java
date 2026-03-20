package request_management.application;

import buildingblocks.application.InboundPort;
import request_management.domain.Shipment;

@InboundPort
public interface ValidateShipmentRequest {
    boolean validate(Shipment shipment);
}