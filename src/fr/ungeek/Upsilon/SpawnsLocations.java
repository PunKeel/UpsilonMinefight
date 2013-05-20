package fr.ungeek.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * User: PunKeel
 * Date: 5/19/13
 * Time: 11:39 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class SpawnsLocations {
	double x, y, z, yaw;

	public SpawnsLocations(Location location) {
		x = location.getX();
		y = location.getY();
		z = location.getZ();
		yaw = location.getYaw();
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld("world"), x, y, z, (long) yaw, (long) 0x1);
	}
}
