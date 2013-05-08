package fr.ungeek.Upsilon;

import fr.ungeek.Upsilon.events.ChangeMenuEvent;
import fr.ungeek.Upsilon.events.CloseMenuEvent;
import fr.ungeek.Upsilon.events.MenuClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class MenuInventory implements Listener {
	Main m;

	public MenuInventory(Main main) {
		m = main;
	}

	@EventHandler
	public void onClicDroitMenu(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!p.isOp()) return;
		if (e.getItem().getType() != Material.EMERALD) return;
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
		Inventory inv = Bukkit.createInventory(null, 9, "Menu");
		m.current_menu.put(p.getName(), Main.Menus.MAIN);
		inv.setItem(2, m.nameItem(new ItemStack(Material.EMPTY_MAP), ChatColor.DARK_GREEN + "Menu de ouesh téléportation"));
		if (!(p.isOp() || p.getName().equalsIgnoreCase("dleot")))
			inv.setItem(6, m.nameItem(new ItemStack(Material.SMOOTH_STAIRS), ChatColor.DARK_AQUA + "Menu de boutique de zAchats", "HT & VAN D D OBJ"));
		else
			inv.setItem(6, m.nameItem(new ItemStack(Material.SMOOTH_STAIRS), ChatColor.DARK_AQUA + "Magagin", ChatColor.DARK_RED + "Bientôt disponible"));


		p.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (m.current_menu.containsKey(p.getName())) {

			Main.Menus current = m.current_menu.get(p.getName());
			p.sendMessage(m.current_menu.toString());
			CloseMenuEvent event = new CloseMenuEvent(current, p);
			Bukkit.getServer().getPluginManager().callEvent(event);
			m.current_menu.remove(p.getName());
		}
	}

	@EventHandler
	public void OnInventoryClick(InventoryEvent e) {
		for (HumanEntity HE : e.getViewers()) {
			((Player) HE).sendMessage("." + e.toString());
		}

	}

	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		m.print(1);
		if (!(e.getWhoClicked() instanceof Player)) return;
		Player p = (Player) e.getWhoClicked();
		p.sendMessage(m.current_menu.toString());
		if (!m.current_menu.containsKey(p.getName())) return;
		e.setCancelled(true);
		Main.Menus current = m.current_menu.get(p.getName());
		if (current.equals(Main.Menus.MAIN)) {
			if (e.getSlot() == 2) {
				m.current_menu.put(p.getName(), Main.Menus.TELEPORTATION);
				ChangeMenuEvent event = new ChangeMenuEvent(Main.Menus.TELEPORTATION, p);
				Bukkit.getServer().getPluginManager().callEvent(event);
			} else if (e.getSlot() == 6) {
				if (p.isOp()) {
					m.current_menu.put(p.getName(), Main.Menus.SHOP);
					ChangeMenuEvent event = new ChangeMenuEvent(Main.Menus.SHOP, p);
					Bukkit.getServer().getPluginManager().callEvent(event);
				} else {
					p.sendMessage("La boutique est fermée pour le moment.");
				}

			}
		} else {
			p.sendMessage("Clic");
			MenuClickEvent event = new MenuClickEvent(current, p, e);
			Bukkit.getServer().getPluginManager().callEvent(event);
		}

		p.sendMessage(m.current_menu.toString());
	}
}
