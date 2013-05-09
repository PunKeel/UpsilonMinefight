package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.ChangeMenuEvent;
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

	public MainMenu(Main main, MenuManager MM) {
		m = main;
		this.MM = MM;
	}

	public MenuManager.Menus getSelfMenuType() {
		return MenuManager.Menus.MAIN;
	}

	@EventHandler
	public void onMenuOpen(ChangeMenuEvent e) {
		if (!e.getNew_menu().equals(getSelfMenuType())) return;
		Inventory inv = Bukkit.createInventory(null, 9, "Menu");
		MM.current_menu.put(e.getPlayer().getName(), MenuManager.Menus.MAIN);
		inv.setItem(2, m.nameItem(new ItemStack(Material.EMPTY_MAP), ChatColor.DARK_GREEN + "Menu de téléportation"));
		inv.setItem(4, m.nameItem(new ItemStack(Material.SKULL_ITEM, 1, (short) 3), ChatColor.DARK_GRAY + "Gestion des amis", ChatColor.DARK_RED + "Bientôt disponible"));
		if (e.getPlayer().isOp() || e.getPlayer().getName().equalsIgnoreCase("dleot"))
			inv.setItem(6, m.nameItem(new ItemStack(Material.WOOD_SWORD), ChatColor.DARK_AQUA + "Menu du magazin", "Acheter et vendre des items"));
		else
			inv.setItem(6, m.nameItem(new ItemStack(Material.WOOD_SWORD), ChatColor.DARK_AQUA + "Menu du magazin", ChatColor.DARK_RED + "Bientôt disponible"));
		e.getPlayer().openInventory(inv);
	}

	@EventHandler
	public void onMenuClick(MenuClickEvent e) {
		Player p = e.getPlayer();
		if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
		switch (e.getEvent().getSlot()) {
			case 2:
				MM.openInventory(p, MenuManager.Menus.TELEPORTATION);
				break;
			case 6:
				if (p.isOp()) {
					MM.openInventory(p, MenuManager.Menus.SHOP);
				} else {
					p.sendMessage("La boutique est fermée pour le moment.");
				}
				break;

			default:
				return;

		}
	}
}
