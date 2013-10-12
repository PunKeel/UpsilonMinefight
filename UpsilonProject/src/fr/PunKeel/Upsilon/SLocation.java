package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SLocation {

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

    public SLocation(String world, int x, int y, int z, int yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public String getWorld() {
        return world;
    }

    public Location toLocation() {
        if (loc == null)
            loc = new Location(Bukkit.getWorld(world), x, y, z, (long) yaw, (long) 0x1);
        return loc;
    }
}
