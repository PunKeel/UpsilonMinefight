package fr.PunKeel.Upsilon;

import org.bukkit.Location;

import java.util.*;

public class SpawnManager {
    private HashMap<String, ArrayList<SLocation>> respawns = new HashMap<String, ArrayList<SLocation>>();

    public void addRespawn(String region, Location loc) {
        SLocation l = new SLocation(loc);
        addRespawn(region, l);
    }

    void addRespawn(String region, SLocation loc) {
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

    public SLocation getRandom(String region, final Location loc) {
        if (!exists(region)) return null;
        ArrayList<SLocation> sLocations = respawns.get(region);
        Collections.sort(sLocations, new Comparator<SLocation>() {
            @Override
            public int compare(SLocation o1, SLocation o2) {
                return (int) Math.round(o1.toLocation().distanceSquared(loc) - o2.toLocation().distanceSquared(loc));
            }
        }); // Sort by distance to `loc`
        SLocation[] spawns = sLocations.toArray(new SLocation[sLocations.size()]);
        int id = new Random().nextInt(Math.min(spawns.length, 3)); // Select one of 3 nearest locations
        return spawns[id];
    }

    public void removeSpawn(String arg, int id) {
        if (!respawns.containsKey(arg)) return;
        if (respawns.get(arg).size() < id) return;
        ArrayList<SLocation> var = respawns.get(arg);
        respawns.get(arg).remove(var.toArray(new SLocation[var.size()])[id]);
    }

}