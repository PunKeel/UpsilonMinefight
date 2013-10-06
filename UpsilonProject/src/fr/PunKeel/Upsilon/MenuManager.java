package fr.PunKeel.Upsilon;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.StateFlag;
import fr.PunKeel.Upsilon.events.MenuChangeEvent;
import fr.PunKeel.Upsilon.events.MenuClickEvent;
import fr.PunKeel.Upsilon.events.MenuCloseEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
@SuppressWarnings("ALL")
public class MenuManager implements Listener {

    public static final StateFlag FLAG_EMERAUDE = new StateFlag("emeraude", true);
    public HashMap<String, Menus> current_menu;
    Main m;

    public MenuManager(Main main) {
        m = main;
        current_menu = new HashMap<String, Menus>();
    }

    public void init() {
        m.getWGCF().addCustomFlag(FLAG_EMERAUDE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onClicDroitMenu(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!m.canUse(p)) return;
        if (e.getItem() == null) return;
        if (e.getItem().getType() != Material.EMERALD) return;
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }
        if (!WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation()).allows(FLAG_EMERAUDE)) {
            p.sendMessage(Main.getTAG() + ChatColor.DARK_RED + "Interdit d'utiliser l'émeraude ici !");
            return;
        }
        e.setCancelled(true);
        openInventory(p, Menus.TELEPORTATION);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (e.getView().getTopInventory().getTitle().equalsIgnoreCase("poubelle publique")) {
            e.getView().getTopInventory().clear();
        }
        HumanEntity p = e.getPlayer();
        if (current_menu.containsKey(p.getName())) {
            Menus current = current_menu.get(p.getName());
            MenuCloseEvent event = new MenuCloseEvent(current, p);
            Bukkit.getServer().getPluginManager().callEvent(event);
        }
    }

    public void closeInventory(final HumanEntity p) {
        p.closeInventory();
    }

    public void closeAll() {
        for (String p : current_menu.keySet()) {
            Bukkit.getPlayerExact(p).closeInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClose(MenuCloseEvent e) {
        current_menu.remove(e.getPlayer().getName());
    }

    public void openInventory(final HumanEntity p, final Menus kind) {
        if (current_menu.containsKey(p.getName()))
            closeInventory(p);
        current_menu.put(p.getName(), kind);
        Bukkit.getScheduler().scheduleAsyncDelayedTask(m, new Runnable() {
            @Override
            public void run() {
                MenuChangeEvent event = new MenuChangeEvent(kind, p);
                Bukkit.getServer().getPluginManager().callEvent(event);
            }
        }, 1);
    }

    @EventHandler(ignoreCancelled = true)
    public void OnInventoryClick(final InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        try {
            if (!current_menu.containsKey(p.getName()) && e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.MAGIC + "" + ChatColor.RESET) && !e.getCurrentItem().getType().equals(Material.EMERALD))
                e.setCurrentItem(null);
        } catch (NullPointerException ignored) {

        }

        if (!e.getInventory().getType().equals(InventoryType.CHEST)) return;
        if (e.getSlot() == -999) return;
        if (!current_menu.containsKey(p.getName())) return;
        final Menus current = current_menu.get(p.getName());
        if (current.equals(Menus.ENCHANTING)) return;
        e.setCancelled(true);

        p.playEffect(p.getLocation(), Effect.CLICK1, 1);
        Bukkit.getScheduler().scheduleAsyncDelayedTask(m, new Runnable() {
            @Override
            public void run() {
                try {
                    MenuClickEvent event = new MenuClickEvent(current, e);
                    Bukkit.getServer().getPluginManager().callEvent(event);
                } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                    Main.print("ERREUR java.lang.ArrayIndexOutOfBoundsException AVEC " + current);
                }
            }
        }, 0);

    }

    public void openEnderChest(final HumanEntity p) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(m, new Runnable() {
            @Override
            public void run() {
                p.openInventory(p.getEnderChest());
            }
        }, 0);
    }

    public enum Menus {
        MAIN, TELEPORTATION, SHOP, EVENTS, ENCHANTING;

        public static boolean contains(String s) {
            for (Menus choix : values())
                if (choix.name().equals(s))
                    return true;
            return false;
        }
    }
}

