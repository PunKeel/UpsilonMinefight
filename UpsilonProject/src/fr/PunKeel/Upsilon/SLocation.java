package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SLocation {
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    private double x;
    private double y;
    private double z;
    private double yaw;
    private String world;
    private Location loc = null;

    public SLocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        world = location.getWorld().getName();
        yaw = location.getYaw();
    }

    public Location toLocation() {
        if (loc == null)
            loc = new Location(Bukkit.getWorld(world), x, y, z, (long) yaw, (long) 0x1);
        return loc;
    }
}
