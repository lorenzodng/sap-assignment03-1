package drone_management.application;

import drone_management.domain.Drone;

public class CheckDroneAvailabilityImpl implements CheckDroneAvailability {

    private static final int MAX_DELIVERY_TIME_MINUTES = 60;

    @Override
    public boolean check(Drone drone, double packageWeight, double distanceDroneToPickup, double distancePickupToDelivery) {

        //verifica capacita di peso
        if (drone.getWeightCapacity() < packageWeight) {
            return false;
        }

        //verifica disponibilita batteria
        double availableBattery = drone.getBatteryCapacity() - Drone.SAFETY_BATTERY;
        double requiredBattery = (distanceDroneToPickup + distancePickupToDelivery) * Drone.BATTERY_CONSUMPTION_PER_KM;
        if (availableBattery < requiredBattery) {
            return false;
        }

        //verifica tempo di consegna
        double deliveryTimeMinutes = ((distanceDroneToPickup + distancePickupToDelivery) / Drone.SPEED) * 60;
        if(deliveryTimeMinutes > MAX_DELIVERY_TIME_MINUTES) {
            return false;
        }

        return true;
    }
}