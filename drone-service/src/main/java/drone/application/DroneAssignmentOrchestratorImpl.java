package drone.application;

import drone.domain.Drone;
import drone.domain.GeoUtils;
import java.util.List;

//orchestratore che coordina il flusso principale di assegnazione di un drone
public class DroneAssignmentOrchestratorImpl implements DroneAssignmentOrchestrator {

    private final AssignDrone assignDrone;
    private final DroneEventProducer eventProducer;
    private final DroneRepository repository;

    public DroneAssignmentOrchestratorImpl(AssignDrone assignDrone, DroneEventProducer eventProducer, DroneRepository repository) {
        this.assignDrone = assignDrone;
        this.eventProducer = eventProducer;
        this.repository = repository;
    }

    //gestisce l'assegnazione del drone alla spedizione
    @Override
    public void handleShipmentRequested(String shipmentId, double pickupLat, double pickupLon, double deliveryLat, double deliveryLon, double weight, int timeLimit) {
        try {
            //step 1: assegna il drone
            double distance = GeoUtils.haversine(pickupLat, pickupLon, deliveryLat, deliveryLon);
            List<Drone> drones = repository.findAll(); //recupera tutti i droni esistenti
            Drone assignedDrone = assignDrone.assign(drones, weight, pickupLat, pickupLon, distance, timeLimit);
            repository.updateAvailability(assignedDrone.getId(), false); //imposta il drone come non più disponibile

            //step 2: pubblica l'evento
            eventProducer.publishDroneAssigned(shipmentId, assignedDrone, pickupLat, pickupLon, deliveryLat, deliveryLon);
        } catch (DroneNotAvailableException e) { //se non esiste un drone disponibile
            eventProducer.publishDroneNotAvailable(shipmentId);
        }
    }
}