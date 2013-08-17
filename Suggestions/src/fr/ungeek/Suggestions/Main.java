package fr.ungeek.Suggestions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.ItemStack;
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
    }

    @Override
    public List<Class<?>> getDatabaseClasses() {
        final List<Class<?>> list = new java.util.ArrayList<>(1);
        list.add(Suggestion.class);

        return list;
    }

    @EventHandler()
    public void onSuggestion(InventoryMoveItemEvent e) {
        if (!e.getDestination().getTitle().equalsIgnoreCase("sugg")) return;
        ItemStack livre = e.getItem();
        if (livre.getType().equals(Material.WRITTEN_BOOK) || livre.getType().equals(Material.BOOK_AND_QUILL)) {
            BookMeta bmeta = (BookMeta) livre.getItemMeta();
            if (livre.getType().equals(Material.BOOK_AND_QUILL)) {
                bmeta.setTitle("Sans titre");
                bmeta.setAuthor("Anonyme");
            }
            Suggestion s = new Suggestion(bmeta.getAuthor(), Main.getTimestamp(), bmeta.getTitle(), bmeta.getPages());
            getDatabase().save(s);
            e.setItem(null);
            broadcastToAdmins(getTAG() + "Nouvelle suggestion de " + ChatColor.DARK_GREEN + bmeta.getAuthor() + ChatColor.RESET + " : " + ChatColor.GOLD + bmeta.getTitle());
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
