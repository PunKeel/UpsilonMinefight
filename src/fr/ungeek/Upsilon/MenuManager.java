package fr.ungeek.Upsilon;

import fr.ungeek.Upsilon.events.ChangeMenuEvent;
import fr.ungeek.Upsilon.events.CloseMenuEvent;
import fr.ungeek.Upsilon.events.MenuClickEvent;
import org.bukkit.Bukkit;
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
		if (!m.canUse(p)) return;
		if (e.getItem() == null) return;
		if (e.getItem().getType() != Material.EMERALD) return;
		if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
		e.setCancelled(true);
		openInventory(p, Menus.MAIN);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		if (e.getView().getTopInventory().getTitle().equalsIgnoreCase("poubelle publique")) {
			e.getView().getTopInventory().clear();
		}
		Player p = (Player) e.getPlayer();
		if (current_menu.containsKey(p.getName())) {
			Menus current = current_menu.get(p.getName());
			CloseMenuEvent event = new CloseMenuEvent(current, p);
			Bukkit.getServer().getPluginManager().callEvent(event);
		}
	}

	public void closeInventory(Menus current, HumanEntity p) {
		CloseMenuEvent event = new CloseMenuEvent(current, p);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onClose(CloseMenuEvent e) {
		e.getPlayer().closeInventory();
		current_menu.remove(e.getPlayer().getName());
	}

	public void openInventory(final HumanEntity p, final Menus kind) {
		if (current_menu.containsKey(p.getName()))
			closeInventory(current_menu.get(p.getName()), p);
		current_menu.put(p.getName(), kind);
		Bukkit.getScheduler().scheduleAsyncDelayedTask(m, new Runnable() {
			@Override
			public void run() {
				ChangeMenuEvent event = new ChangeMenuEvent(kind, p);
				Bukkit.getServer().getPluginManager().callEvent(event);
			}
		}, 2);
	}

	@EventHandler
	public void OnInventoryClick(InventoryClickEvent e) {
		if (e.getSlot() == -999) return;
		Player p = (Player) e.getWhoClicked();
		if (!current_menu.containsKey(p.getName())) return;
		Menus current = current_menu.get(p.getName());
		e.setCancelled(true);
		if (current.equals(Menus.ENCHANTING)) {
			e.setCancelled(false);
		}
		p.playEffect(p.getLocation(), Effect.CLICK1, 1);
		MenuClickEvent event = new MenuClickEvent(current, e);
		Bukkit.getServer().getPluginManager().callEvent(event);
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

