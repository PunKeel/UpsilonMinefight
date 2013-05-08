package fr.ungeek.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

/**
 * User: PunKeel
 * Date: 5/8/13
 * Time: 6:16 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class EventMenu implements Listener {
	Main m;
	ArrayList<String> viewing = new ArrayList<String>();

	public EventMenu(Main main) {
		m = main;
	}

	public void openMenuForPlayer(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, "Menu > Téléportation > Events");
		viewing.add(p.getName());
		inv.setItem(1, m.nameItem(new ItemStack(Material.EMERALD_BLOCK), ChatColor.DARK_RED + "Spawn", "Aller simple"));
		inv.setItem(3, m.nameItem(new ItemStack(Material.GRASS), ChatColor.BLUE + "Map normale"));
		inv.setItem(5, m.nameItem(new ItemStack(Material.NETHERRACK), ChatColor.DARK_GREEN + "Nether"));
		inv.setItem(7, m.nameItem(new ItemStack(Material.COMPASS), ChatColor.DARK_AQUA + "Events"));
		p.openInventory(inv);
	}
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		ItemStack[] inv = e.getView().getTopInventory().getContents();
		if (!viewing.contains(p.getName())) return;
		viewing.remove(p.getName());

	}

	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		if (!viewing.contains(p.getName())) return;
		e.setCancelled(true);
		String warp = "";
		switch (e.getSlot()) {
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
		viewing.remove(p.getName());
		p.closeInventory();
		if (warp.equalsIgnoreCase("event")) {
			m.EM.openMenuForPlayer(p);
		} else {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("warp %s %s", warp, p.getName()));
		}

	}
}
