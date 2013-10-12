package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

/**
 * User: PunKeel
 * Date: 10/12/13
 * Time: 4:04 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Halloween {
    public HashSet<String> locations;
    Main main;

    public Halloween(Main main) {
        this.main = main;
    }

    @CommandController.CommandHandler(name = "citrouille", permission = "upsilon.citrouille", usage = "/citrouille")
    public void onCitrouille(Player p, String[] args) {
        p.sendMessage("Citrouille ajoutée ! :)");
        locations.add(getLocation(p.getLocation()));
    }

    @CommandController.CommandHandler(name = "Halloween", permission = "upsilon.Halloween", usage = "/Halloween")
    public void onHalloween(Player p, String[] args) {
        Location l = randomLocation();
        p.sendMessage("Citrouille spawn : " + l.toString());
        Bukkit.getWorld(Main.WORLDNAME).dropItem(l, new ItemStack(Material.PUMPKIN));
    }

    String getLocation(Location b) {
        return b.getBlockX() + "_" + b.getBlockY() + "_" + b.getBlockZ();
    }

    Location randomLocation() {
        String random = Main.getRandom(locations);
        if (random == null || random.isEmpty()) {
            return Bukkit.getWorld(Main.WORLDNAME).getSpawnLocation();
        }
        String[] coords = random.split("_");
        return new Location(Bukkit.getWorld(Main.WORLDNAME), Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
    }

}
