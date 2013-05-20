package fr.ungeek.Upsilon;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

/**
 * User: PunKeel
 * Date: 5/9/13
 * Time: 5:49 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Arenas {
	Main main;
	Integer in_nether = 0;
	Integer in_normal = 0;
	ProtectedRegion normale;
	ProtectedRegion nether;
	Sign sign_nether, sign_normal;

	public Arenas(final Main main) {
		this.main = main;
		nether = main.RM.getRegion("arene2");
		normale = main.RM.getRegion("arene1");

		Block temp_sign;

		temp_sign = Bukkit.getWorld("world").getBlockAt(7, 53, 275);
		if (temp_sign.getType().equals(Material.WALL_SIGN))
			sign_nether = (Sign) temp_sign.getState();


		temp_sign = Bukkit.getWorld("world").getBlockAt(7, 53, 261);
		if (temp_sign.getType().equals(Material.WALL_SIGN))
			sign_normal = (Sign) temp_sign.getState();
		if (sign_normal != null) {
			sign_normal.setLine(0, "Il y a");
			sign_normal.setLine(1, "-1 joueur");
			sign_normal.setLine(2, "dans l'arène");
			sign_normal.setLine(3, "Normale");
			sign_normal.update();
		}
		if (sign_nether != null) {
			sign_nether.setLine(0, "Il y a");
			sign_nether.setLine(1, "-1 joueur");
			sign_nether.setLine(2, "dans l'arène");
			sign_nether.setLine(3, "Nether");
			sign_nether.update();
		}
		final Arenas self = this;
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(main, new Runnable() {
			@Override
			public void run() {
				in_nether = 0;
				in_normal = 0;
				for (Player p : Bukkit.getOnlinePlayers()) {
					Location loc = p.getLocation();
					if (isInNether(loc)) {
						in_nether++;
					} else if (isInNormal(loc)) {
						in_normal++;
					}
				}
				if (sign_normal != null) {
					sign_normal.setLine(1, in_normal + " joueur" + ((in_normal > 1) ? "s" : ""));
					sign_normal.update(true);
				}
				if (sign_nether != null) {
					sign_nether.setLine(1, in_nether + " joueur" + ((in_nether > 1) ? "s" : ""));
					sign_nether.update(true);
				}
			}
		}, 20 * 10, 20 * 3);
	}

	public boolean isInNether(Location loc) {
		return (nether.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}

	public boolean isInNormal(Location loc) {
		return (normale.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}

	public Integer getIn_normal() {
		return in_normal;
	}

	public Integer getIn_nether() {
		return in_nether;
	}
}
