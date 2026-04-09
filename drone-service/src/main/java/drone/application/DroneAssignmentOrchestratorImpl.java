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

        // Calcolo distanza (logica di business)
        double distance = calculateDistanceInKm(pickupLat, pickupLon, deliveryLat, deliveryLon);
        List<Drone> drones = repository.findAll(); //recupera tutti i droni esistenti
        Drone assignedDrone = assignDrone.assign(drones, weight, pickupLat, pickupLon, distance, timeLimit);

        if (assignedDrone != null) {
            repository.updateAvailability(assignedDrone.getId(), false); //imposta il drone come non più disponibile
            eventProducer.publishDroneAssigned(shipmentId, assignedDrone, pickupLat, pickupLon, deliveryLat, deliveryLon);
        } else {
            eventProducer.publishDroneNotAvailable(shipmentId);
        }
    }

    //calcola la distanza tra il luogo di pickup e il luogo di destinazione
    private double calculateDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        return GeoUtils.haversine(lat1, lon1, lat2, lon2);
    }
}