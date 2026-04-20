package drone.application;

import drone.domain.Drone;
import drone.domain.GeoUtils;
import java.util.List;

public class DroneAssignmentOrchestratorImpl implements DroneAssignmentOrchestrator {

    private final AssignDrone assignDrone;
    private final DroneEventProducer eventProducer;
    private final DroneRepository repository;

    public DroneAssignmentOrchestratorImpl(AssignDrone assignDrone, DroneEventProducer eventProducer, DroneRepository repository) {
        this.assignDrone = assignDrone;
        this.eventProducer = eventProducer;
        this.repository = repository;
    }

    @Override
    public void handleShipmentRequested(String shipmentId, double pickupLat, double pickupLon, double deliveryLat, double deliveryLon, double weight, int timeLimit) {
        try {
            //step 1: assign a drone
            double distance = GeoUtils.haversine(pickupLat, pickupLon, deliveryLat, deliveryLon);
            List<Drone> drones = repository.findAll();
            Drone assignedDrone = assignDrone.assign(drones, weight, pickupLat, pickupLon, distance, timeLimit);
            repository.updateAvailability(assignedDrone.getId(), false);

            //step 2: publish the event
            eventProducer.publishDroneAssigned(shipmentId, assignedDrone, pickupLat, pickupLon, deliveryLat, deliveryLon);
        } catch (DroneNotAvailableException e) {
            eventProducer.publishDroneNotAvailable(shipmentId);
        }
    }
}