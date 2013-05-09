package fr.ungeek.Upsilon;

import fr.ungeek.Upsilon.events.ChangeMenuEvent;
import fr.ungeek.Upsilon.events.CloseMenuEvent;
import fr.ungeek.Upsilon.events.MenuClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class MenuManager implements Listener {

	public HashMap<String, Menus> current_menu = new HashMap<String, Menus>();
	Main m;

	public MenuManager(Main main) {
		m = main;
	}

	@EventHandler
	public void onClicDroitMenu(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (!p.isOp()) return;
		if (e.getItem().getType() == null) return;
		if (e.getItem().getType() != Material.EMERALD) return;
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
		openInventory(p, Menus.MAIN);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (current_menu.containsKey(p.getName())) {
			Menus current = current_menu.get(p.getName());
			CloseMenuEvent event = new CloseMenuEvent(current, p);
			Bukkit.getServer().getPluginManager().callEvent(event);
			current_menu.remove(p.getName());
		}
	}

	public void openInventory(Player p, Menus kind) {
		final Player player = p;
		final Menus type = kind;
		current_menu.put(p.getName(), kind);
		p.closeInventory();
		Bukkit.getScheduler().scheduleSyncDelayedTask(m, new Runnable() {
			@Override
			public void run() {
				ChangeMenuEvent event = new ChangeMenuEvent(type, player);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}
		}, 2);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void OnInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if (!current_menu.containsKey(p.getName())) return;
		if (!e.getInventory().equals(e.getView().getTopInventory())) return;
		if (e.getSlot() == -999) return;
		p.playEffect(p.getLocation(), Effect.CLICK1, 1);
		e.setCancelled(true);
		Menus current = current_menu.get(p.getName());
		MenuClickEvent event = new MenuClickEvent(current, p, e);
		Bukkit.getServer().getPluginManager().callEvent(event);

	}

	public enum Menus {
		MAIN, TELEPORTATION, SHOP, EVENTS;

		public static boolean contains(String s) {
			for (Menus choix : values())
				if (choix.name().equals(s))
					return true;
			return false;
		}
	}
}

