package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

public class SLocation {
    double x, y, z, yaw;
    Location loc = null;

    public SLocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
    }

    public Location toLocation() {
        if (loc == null)
            loc = new Location(Bukkit.getWorld(Main.WORLDNAME), x, y, z, (long) yaw, (long) 0x1);
        return loc;
    }
}
