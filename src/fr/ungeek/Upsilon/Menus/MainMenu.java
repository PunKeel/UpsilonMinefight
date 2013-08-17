package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.MenuChangeEvent;
import fr.ungeek.Upsilon.events.MenuClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
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
public class MainMenu implements Listener {
    Main m;
    MenuManager MM;
    ItemStack teleport, magasin;
    Inventory I = Bukkit.createInventory(null, 9, "Menu");

    public MainMenu(Main main, MenuManager MM) {
        m = main;
        this.MM = MM;
        teleport = m.nameItem(new ItemStack(Material.COMPASS), ChatColor.DARK_GREEN + "Menu de téléportation", "Pour se déplacer + vite");
        magasin = m.nameItem(new ItemStack(Material.WOOD_SWORD), ChatColor.DARK_AQUA + "Menu du magasin", "Vendre des items", ChatColor.DARK_RED + "Bientôt disponible");
        //amis = m.nameItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.DARK_GRAY + "Gestionnaire d'amis", ChatColor.DARK_RED + "Bientôt disponible", "Gérez vos amis !");

        I.setItem(2, teleport);
        I.setItem(6, magasin);
    }

    public MenuManager.Menus getSelfMenuType() {
        return MenuManager.Menus.MAIN;
    }

    @EventHandler
    public void onMenuOpen(MenuChangeEvent e) {
        if (!e.getNew_menu().equals(getSelfMenuType())) return;

        MM.current_menu.put(e.getPlayer().getName(), MenuManager.Menus.MAIN);
        //inv.setItem(6, amis);
        e.getPlayer().openInventory(I);
    }

    @EventHandler
    public void onMenuClick(MenuClickEvent e) {
        if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
        Player p = (Player) e.getEvent().getWhoClicked();
        switch (e.getEvent().getSlot()) {
            case 2:
                MM.openInventory(p, MenuManager.Menus.TELEPORTATION);
                return;
            /*case 6:
                if (m.isAdmin(p)) {
                    MM.openInventory(p, MenuManager.Menus.SHOP);
                } else {
                    p.sendMessage(Main.getTAG() + "Le gestionnaire d'amis est fermé pour le moment.");
                }
                return; */
            case 6:
                if (m.isAdmin(p)) {
                    MM.openInventory(p, MenuManager.Menus.SHOP);
                } else {
                    p.sendMessage(Main.getTAG() + "La boutique est fermée pour le moment.");
                }
                return;
            default:

        }
    }

}
