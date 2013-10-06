package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 8/17/13
 * Time: 7:01 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
class Boussole implements Listener {
    private HashMap<String, String> focus = new HashMap<>();

    public Boussole(Main main) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(main, new Runnable() {

            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getInventory().contains(Material.COMPASS)) {
                        updateCompass(p);
                    }
                }
            }
        }, 20L, 20 * 20L);
    }

    void updateCompass(Player p) {
        Player cible;
        if (focus.containsKey(p.getName())) {
            cible = Bukkit.getPlayer(focus.get(p.getName()));
            if (cible != null && cible.isOnline()) {
                p.setCompassTarget(cible.getLocation());
                return;
            }
        }
        cible = findNearestPlayer(p);
        if (cible == null) {
            p.setCompassTarget(p.getWorld().getSpawnLocation());
        } else {
            p.setCompassTarget(cible.getLocation());
        }

    }

    Player findNearestPlayer(Player p) {
        Location ploc = p.getLocation();
        Player nearest = null;
        double distance = Integer.MAX_VALUE, newd;
        for (Player x : Bukkit.getOnlinePlayers()) {
            if (x.getName().equals(p.getName())) continue;
            newd = ploc.distanceSquared(x.getLocation());
            if (distance > newd) {
                nearest = x;
                distance = newd;
            }
        }
        return nearest;
    }

    public void setFocus(Player cs, String arg) {
        if (arg == null) {
            if (focus.containsKey(cs.getName()))
                focus.remove(cs.getName());
        } else {
            focus.put(cs.getName(), arg);
            updateCompass(cs);
        }
    }
}
