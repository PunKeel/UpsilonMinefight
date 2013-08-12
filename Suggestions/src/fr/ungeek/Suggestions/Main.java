package fr.ungeek.Suggestions;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * User: PunKeel
 * Date: 6/15/13
 * Time: 4:01 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin implements Listener {
    ProtectedRegion suggestion_drop;

    public static String getTAG() {
        return ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Minefight" + ChatColor.DARK_GREEN + "] " + ChatColor.RESET;
    }

    public static Integer getTimestamp() {
        return (int) (System.nanoTime() / 1000000000);
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            getDatabase().find(Suggestion.class).findRowCount();
        } catch (javax.persistence.PersistenceException ex) {
            getLogger().info("Mise en place de la BDD pour UpsilonMinefight");
            installDDL();
        }
        World world = Bukkit.getWorld("world");
        RegionManager RM = WGBukkit.getRegionManager(world);
        suggestion_drop = RM.getRegion("suggestion_drop");
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        final List<Class<?>> list = new java.util.ArrayList<>(1);
        list.add(Suggestion.class);

        return list;
    }

    @EventHandler()
    public void onSuggestion(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (suggestion_drop.contains(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()))
            if (e.getItemDrop().getItemStack().getType().equals(Material.WRITTEN_BOOK)) {
                BookMeta bmeta = (BookMeta) e.getItemDrop().getItemStack().getItemMeta();
                Suggestion s = new Suggestion(bmeta.getAuthor(), Main.getTimestamp(), bmeta.getTitle(), bmeta.getPages());
                getDatabase().save(s);
                e.getItemDrop().remove();
                p.sendMessage(getTAG() + "Suggestion reçue, elle sera traitée aussitôt que possible !");
                broadcastToAdmins(getTAG() + "Nouvelle suggestion de " + ChatColor.DARK_GREEN + p.getName() + ChatColor.RESET + " : " + ChatColor.GOLD + bmeta.getTitle());
            }
    }

    public void broadcastToAdmins(String o) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("upsilon.admin.see")) {
                p.sendMessage(o);
            }
        }
    }


}
