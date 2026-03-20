package drone_management.domain;

import buildingblocks.domain.Entity;

public class Drone implements Entity<String> {

    public static final double BATTERY_CONSUMPTION_PER_KM = 2.0;
    public static final double SAFETY_BATTERY = 10.0;
    public static final double SPEED = 50.0;
    private final String id;
    private final double batteryCapacity;
    private final double weightCapacity;
    private Position position;
    private boolean available;

    public Drone(String id, double batteryCapacity, double weightCapacity, Position position) {
        this.id = id;
        this.batteryCapacity = batteryCapacity;
        this.weightCapacity = weightCapacity;
        this.position = position;
        this.available = true;
    }

    @Override
    public String getId() {
        return id;
    }

    public double getBatteryCapacity() {
        return batteryCapacity;
    }

    public double getWeightCapacity() {
        return weightCapacity;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isAvailable() {
        return available;
    }

    public void updatePosition(Position newPosition) {
        this.position = newPosition;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
