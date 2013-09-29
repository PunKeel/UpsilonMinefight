package fr.PunKeel.Upsilon.Menus;

import fr.PunKeel.Upsilon.Main;
import fr.PunKeel.Upsilon.MenuManager;
import fr.PunKeel.Upsilon.events.MenuChangeEvent;
import fr.PunKeel.Upsilon.events.MenuClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class TeleportationMenu implements Listener {
    private Main m;
    private MenuManager MM;
    private Inventory vip = Bukkit.createInventory(null, 9, "Menu > Téléportation");
    private Inventory notvip = Bukkit.createInventory(null, 9, "Menu > Téléportation");

    public TeleportationMenu(Main main, MenuManager MM) {
        m = main;
        this.MM = MM;
        ItemStack spawn = m.nameItem(new ItemStack(Material.getMaterial(155)), ChatColor.DARK_PURPLE + "Spawn");
        ItemStack events = m.nameItem(new ItemStack(Material.EMPTY_MAP), ChatColor.DARK_AQUA + "Events");
        ItemStack enchant_notvip = m.nameItem(new ItemStack(Material.ENCHANTMENT_TABLE), ChatColor.GOLD + "Table d'enchantements", ChatColor.DARK_RED + "VIP seulement !");
        ItemStack enchant_vip = m.nameItem(new ItemStack(Material.ENCHANTMENT_TABLE), ChatColor.GOLD + "Table d'enchantements", ChatColor.DARK_GREEN + "Cliquez pour accéder !");
        ItemStack enderchest_notvip = m.nameItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Ender Chest", ChatColor.DARK_RED + "VIP seulement !");
        ItemStack enderchest_vip = m.nameItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Ender Chest", ChatColor.DARK_GREEN + "Cliquez pour accéder !");

        //


        vip.setItem(1, spawn);
        notvip.setItem(1, spawn);
        vip.setItem(3, events);
        notvip.setItem(3, events);
        vip.setItem(5, enderchest_vip);
        vip.setItem(7, enchant_vip);
        notvip.setItem(5, enderchest_notvip);
        notvip.setItem(7, enchant_notvip);

    }

    MenuManager.Menus getSelfMenuType() {
        return MenuManager.Menus.TELEPORTATION;
    }

    @EventHandler
    public void onMenuOpen(MenuChangeEvent e) {
        if (!e.getNew_menu().equals(getSelfMenuType())) return;
        MM.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
        HumanEntity p = e.getPlayer();

        if (m.isVIP(p.getName())) {
            e.getPlayer().openInventory(vip);
        } else {
            e.getPlayer().openInventory(notvip);
        }
    }

    @EventHandler
    public void onMenuClick(MenuClickEvent e) {
        if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
        final HumanEntity p = e.getEvent().getWhoClicked();
        final String warp;
        switch (e.getEvent().getSlot()) {
            case 1:
                warp = "spawn";
                break;
            case 3:
                MM.openInventory(p, MenuManager.Menus.EVENTS);
                return;
            case 5:
                if (m.isVIP(p.getName())) {
                    MM.openEnderChest(p);
                    return;
                } else {
                    ((Player) p).sendMessage(Main.getTAG() + "Il faut être VIP pour l'utiliser !");
                    return;
                }
            case 7:
                if (m.isVIP(p.getName())) {
                    warp = "enchant";
                } else {
                    ((Player) p).sendMessage(Main.getTAG() + "Il faut être VIP pour l'utiliser !");
                    return;
                }
                break;
            default:
                return;

        }
        MM.closeInventory(p);
        if (warp.equalsIgnoreCase("event")) {
            MM.openInventory(p, MenuManager.Menus.EVENTS);
        } else if (warp.equalsIgnoreCase("enchant")) {
            MM.openInventory(p, MenuManager.Menus.ENCHANTING);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
                @Override
                public void run() {
                    m.teleportToWarp(warp, p);
                }
            });
        }
    }
}
