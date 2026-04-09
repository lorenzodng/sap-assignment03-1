package request.application;

import buildingblocks.application.OutboundPort;
import io.vertx.core.Future;
import request.domain.Shipment;

@OutboundPort
public interface ShipmentRequestEventProducer {
    Future<Void> publishShipmentRequested(Shipment shipment);
}