package fr.ungeek.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class SpawnManager {
    public HashMap<String, ArrayList<SLocation>> respawns = new HashMap<String, ArrayList<SLocation>>();

    public void addRespawn(String region, Location loc) {
        SLocation l = new SLocation(loc);
        addRespawn(region, l);
    }

    public void addRespawn(String region, SLocation loc) {
        if (!respawns.containsKey(region)) {
            respawns.put(region, new ArrayList<SLocation>());
        }
        respawns.get(region).add(loc);
    }

    public SLocation[] get(String region) {
        if (!respawns.containsKey(region)) return null;
        ArrayList<SLocation> var = respawns.get(region);
        return var.toArray(new SLocation[var.size()]);
    }

    public boolean exists(String region) {
        return respawns.containsKey(region);
    }

    public SLocation getRandom(String region) {
        if (!exists(region)) return null;
        ArrayList<SLocation> sLocations = respawns.get(region);
        SLocation[] spawns = sLocations.toArray(new SLocation[sLocations.size()]);
        int id = new Random().nextInt(spawns.length);
        return spawns[id];
    }

    public void removeSpawn(String arg, int id) {
        if (!respawns.containsKey(arg)) return;
        if (respawns.get(arg).size() < id) return;
        ArrayList<SLocation> var = respawns.get(arg);
        respawns.get(arg).remove(var.toArray(new SLocation[var.size()])[id]);
    }
}

class SLocation {
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