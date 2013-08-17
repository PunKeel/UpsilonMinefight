package fr.ungeek.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SLocation {
    double x, y, z, yaw;

    public SLocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(Main.WORLDNAME), x, y, z, (long) yaw, (long) 0x1);
    }
}
