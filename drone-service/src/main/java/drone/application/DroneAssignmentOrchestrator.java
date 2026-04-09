package drone.application;

import buildingblocks.application.InboundPort;

@InboundPort
public interface DroneAssignmentOrchestrator {
    void handleShipmentRequested(String shipmentId, double pLat, double pLon, double dLat, double dLon, double weight, int limit);
}