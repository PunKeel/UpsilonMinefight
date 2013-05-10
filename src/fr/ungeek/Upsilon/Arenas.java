package fr.ungeek.Upsilon;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

	public Arenas(final Main main) {
		this.main = main;
		nether = main.RM.getRegion("arene2");
		normale = main.RM.getRegion("arene1");
		final Arenas self = this;
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(main, new Runnable() {
			@Override
			public void run() {
				in_nether = 0;
				in_normal = 0;
				for (Player p : Bukkit.getOnlinePlayers()) {
					Location loc = p.getLocation();
					if (nether.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
						in_nether++;
					} else if (normale.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
						in_normal++;
					}
				}
			}
		}, 20 * 10, 20 * 3);
	}

	public Integer getIn_normal() {
		return in_normal;
	}

	public Integer getIn_nether() {
		return in_nether;
	}

}
