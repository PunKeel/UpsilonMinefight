package fr.ungeek.Upsilon;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;

public class SpawnManager {
	public HashMap<String, ArrayList<SpawnsLocations>> respawns = new HashMap<String, ArrayList<SpawnsLocations>>();

	public void addRespawn(String region, Location loc) {
		SpawnsLocations l = new SpawnsLocations(loc);
		addRespawn(region, l);
	}

	public void addRespawn(String region, SpawnsLocations loc) {
		if (!respawns.containsKey(region)) {
			respawns.put(region, new ArrayList<SpawnsLocations>());
		}
		respawns.get(region).add(loc);
	}

	public SpawnsLocations[] get(String region) {
		if (!respawns.containsKey(region)) return null;
		ArrayList<SpawnsLocations> var = respawns.get(region);
		return var.toArray(new SpawnsLocations[var.size()]);
	}

	public void removeSpawn(String arg, int id) {
		if (!respawns.containsKey(arg)) return;
		if (respawns.get(arg).size() < id) return;
		ArrayList<SpawnsLocations> var = respawns.get(arg);
		respawns.get(arg).remove(var.toArray(new SpawnsLocations[var.size()])[id]);
	}
}
