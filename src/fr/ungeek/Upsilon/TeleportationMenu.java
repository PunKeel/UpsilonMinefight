package fr.ungeek.Upsilon;

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
public class TeleportationMenu implements Listener {
	Main m;

	public TeleportationMenu(Main main) {
		m = main;
	}

	public Main.Menus getSelfMenuType() {
		return Main.Menus.TELEPORTATION;
	}

	@EventHandler
	public void onMenuOpen(ChangeMenuEvent e) {
		if (!e.getNew_menu().equals(getSelfMenuType())) return;
		m.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
		boolean show_event_kynset = false;
		show_event_kynset = !m.isBefore(11, 5, 2013);
		Inventory inv = Bukkit.createInventory(null, 9 * (show_event_kynset ? 2 : 1), "Menu > Téléportation");

		inv.setItem(1, m.nameItem(new ItemStack(Material.getMaterial(155)), ChatColor.DARK_PURPLE + "Spawn"));
		inv.setItem(3, m.nameItem(new ItemStack(Material.GRASS), ChatColor.BLUE + "Map normale"));
		inv.setItem(5, m.nameItem(new ItemStack(Material.NETHERRACK), ChatColor.DARK_RED + "Nether"));
		inv.setItem(7, m.nameItem(new ItemStack(Material.COMPASS), ChatColor.DARK_AQUA + "Events"));

		if (show_event_kynset) {
			if (m.isToday(11, 5, 2013)) {
				inv.setItem(13, m.nameItem(new ItemStack(Material.NETHER_STAR), ChatColor.DARK_AQUA + "Event avec Kynset", ChatColor.DARK_RED + "TODAY"));
			} else {
				inv.setItem(13, m.nameItem(new ItemStack(Material.NETHER_STAR), ChatColor.DARK_AQUA + "Event avec Kynset", ChatColor.DARK_RED + "Ouverture le Samedi 11 Mai"));

			}
		}
		e.getPlayer().openInventory(inv);
	}

	@EventHandler
	public void OnInventoryClick(MenuClickEvent e) {
		Player p = e.getPlayer();
		if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
		String warp = "";
		switch (e.getEvent().getSlot()) {
			case 1:
				warp = "spawn";
				break;
			case 3:
				warp = "normal";
				break;
			case 5:
				warp = "nether";
				break;
			case 7:
				warp = "event";
				break;
			case 13:
				if (m.isToday(11, 5, 2013)) {
					warp = "kynset";
				} else {
					p.sendMessage("L'évènement a lieu le samedi 11 Mai !");
					return;
				}
				break;
			default:
				return;

		}
		p.closeInventory();
		if (warp.equalsIgnoreCase("event")) {
			ChangeMenuEvent event = new ChangeMenuEvent(Main.Menus.EVENTS, p);
			Bukkit.getServer().getPluginManager().callEvent(event);
		} else {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("warp %s %s", warp, p.getName()));
		}

	}
}
