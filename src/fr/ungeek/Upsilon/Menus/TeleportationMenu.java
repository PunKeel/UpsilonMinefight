package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.ChangeMenuEvent;
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
	ItemStack spawn, events, enchant_vip, enchant_notvip, kynset_today, kynset_bientot;

	public TeleportationMenu(Main main, MenuManager MM) {
		m = main;
		this.MM = MM;
		spawn = m.nameItem(new ItemStack(Material.getMaterial(155)), ChatColor.DARK_PURPLE + "Spawn");
		events = m.nameItem(new ItemStack(Material.COMPASS), ChatColor.DARK_AQUA + "Events");
		enchant_notvip = m.nameItem(new ItemStack(Material.ENCHANTMENT_TABLE), ChatColor.GOLD + "Salle d'enchantements", ChatColor.DARK_RED + "VIP seulement !");
		enchant_vip = m.nameItem(new ItemStack(Material.ENCHANTMENT_TABLE), ChatColor.GOLD + "Salle d'enchantements", ChatColor.DARK_GREEN + "Cliquez pour accéder !");
		kynset_today = m.nameItem(new ItemStack(Material.NETHER_STAR), ChatColor.DARK_AQUA + "Event avec Kynset", ChatColor.DARK_GREEN + "C'est aujourd'hui !");
		kynset_bientot = m.nameItem(new ItemStack(Material.NETHER_STAR), ChatColor.DARK_AQUA + "Event avec Kynset", ChatColor.DARK_RED + "Ouverture le Samedi 11 Mai");
	}

	public MenuManager.Menus getSelfMenuType() {
		return MenuManager.Menus.TELEPORTATION;
	}

	@EventHandler
	public void onMenuOpen(ChangeMenuEvent e) {
		if (!e.getNew_menu().equals(getSelfMenuType())) return;
		MM.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
		boolean show_event_kynset;
		HumanEntity p = e.getPlayer();
		show_event_kynset = !m.isBefore(11, 5, 2013);
		Inventory inv = Bukkit.createInventory(null, 9 * (show_event_kynset ? 2 : 1), "Menu > Téléportation");

		inv.setItem(0, spawn);
		inv.setItem(2, m.nameItem(new ItemStack(Material.GRASS), ChatColor.BLUE + "Map normale", m.getArenas().getIn_normal() + " joueur" + ((m.getArenas().getIn_normal() > 1) ? "s" : "")));
		inv.setItem(4, m.nameItem(new ItemStack(Material.NETHERRACK), ChatColor.DARK_RED + "Nether", m.getArenas().getIn_nether() + " joueur" + ((m.getArenas().getIn_nether() > 1) ? "s" : "")));
		inv.setItem(6, events);
		if (m.isVIP(p.getName())) {
			inv.setItem(8, enchant_vip);
		} else {
			inv.setItem(8, enchant_notvip);
		}

		if (show_event_kynset) {
			if (m.isToday(11, 5, 2013)) {
				inv.setItem(13, kynset_today);
			} else {
				inv.setItem(13, kynset_bientot);

			}
		}
		e.getPlayer().openInventory(inv);
	}

	@EventHandler
	public void onMenuClick(MenuClickEvent e) {
		if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
		HumanEntity p = e.getEvent().getWhoClicked();
		String warp = "";
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
			case 8:
				if (m.isVIP(p.getName())) {
					warp = "enchant";
				} else {
					((Player) p).sendMessage(m.getTAG() + "Il faut être VIP pour l'utiliser !");
					return;
				}
				break;
			case 13:
				if (m.isToday(11, 5, 2013)) {
					warp = "kynset";
				} else {
					((Player) p).sendMessage(m.getTAG() + "L'événement a lieu le samedi 11 Mai !");
					return;
				}
				break;
			default:
				return;

		}
		MM.closeInventory(this.getSelfMenuType(), p);
		if (warp.equalsIgnoreCase("event")) {
			MM.openInventory(p, MenuManager.Menus.EVENTS);
		} else if (warp.equalsIgnoreCase("enchant")) {
			MM.openInventory(p, MenuManager.Menus.ENCHANTING);
		} else {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("warp %s %s", warp, p.getName()));
		}
	}
}
