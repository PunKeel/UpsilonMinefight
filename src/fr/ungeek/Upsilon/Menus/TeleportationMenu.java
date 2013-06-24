package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.MenuChangeEvent;
import fr.ungeek.Upsilon.events.MenuClickEvent;
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
	Main m;
	MenuManager MM;
	ItemStack spawn, events, enchant_vip, enchant_notvip, enderchest_notvip, enderchest_vip;

	public TeleportationMenu(Main main, MenuManager MM) {
		m = main;
		this.MM = MM;
		spawn = m.nameItem(new ItemStack(Material.getMaterial(155)), ChatColor.DARK_PURPLE + "Spawn");
		events = m.nameItem(new ItemStack(Material.COMPASS), ChatColor.DARK_AQUA + "Events");
		enchant_notvip = m.nameItem(new ItemStack(Material.ENCHANTMENT_TABLE), ChatColor.GOLD + "Table d'enchantements", ChatColor.DARK_RED + "VIP seulement !");
		enchant_vip = m.nameItem(new ItemStack(Material.ENCHANTMENT_TABLE), ChatColor.GOLD + "Table d'enchantements", ChatColor.DARK_GREEN + "Cliquez pour accéder !");
		enderchest_notvip = m.nameItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Ender Chest", ChatColor.DARK_RED + "VIP seulement !");
		enderchest_vip = m.nameItem(new ItemStack(Material.ENDER_CHEST), ChatColor.GOLD + "Ender Chest", ChatColor.DARK_GREEN + "Cliquez pour accéder !");

	}

	public MenuManager.Menus getSelfMenuType() {
		return MenuManager.Menus.TELEPORTATION;
	}

	@EventHandler
	public void onMenuOpen(MenuChangeEvent e) {
		if (!e.getNew_menu().equals(getSelfMenuType())) return;
		MM.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
		HumanEntity p = e.getPlayer();
		Inventory inv = Bukkit.createInventory(null, 9, "Menu > Téléportation");
		int normal, nether;
		normal = m.getArenas().getIn_normal();
		nether = m.getArenas().getIn_nether();
		inv.setItem(0, spawn);
		inv.setItem(2, m.nameItem(new ItemStack(Material.GRASS), ChatColor.BLUE + "Map normale", normal + " joueur" + ((normal > 1) ? "s" : "")));
		inv.setItem(4, m.nameItem(new ItemStack(Material.NETHERRACK), ChatColor.DARK_RED + "Nether", nether + " joueur" + ((nether > 1) ? "s" : "")));
		inv.setItem(6, events);
		if (m.isVIP(p.getName())) {
			inv.setItem(8, enchant_vip);
			inv.setItem(7, enderchest_vip);
		} else {
			inv.setItem(8, enchant_notvip);
			inv.setItem(7, enderchest_notvip);
		}
		e.getPlayer().openInventory(inv);
	}

	@EventHandler
	public void onMenuClick(MenuClickEvent e) {
		if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
		HumanEntity p = e.getEvent().getWhoClicked();
		String warp;
		switch (e.getEvent().getSlot()) {
			case 0:
				warp = "spawn";
				break;
			case 2:
				warp = "normal";
				break;
			case 4:
				warp = "nether";
				break;
			case 6:
				MM.openInventory(p, MenuManager.Menus.EVENTS);
				return;
			case 7:
				if (m.isVIP(p.getName())) {
					MM.openEnderChest(p);
					return;
				} else {
					((Player) p).sendMessage(Main.getTAG() + "Il faut être VIP pour l'utiliser !");
					return;
				}
			case 8:
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
			m.teleportToWarp(warp, p);
		}
	}
}
