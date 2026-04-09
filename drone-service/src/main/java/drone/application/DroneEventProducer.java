package drone.application;

import buildingblocks.application.OutboundPort;
import drone.domain.Drone;

@OutboundPort
public interface DroneEventProducer {
    void publishDroneAssigned(String shipmentId, Drone drone, double pLat, double pLon, double dLat, double dLon);
    void publishDroneNotAvailable(String shipmentId);
}