package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuClickEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private MenuManager.Menus current_menu;
	private Player p;
	private InventoryClickEvent event;

	public MenuClickEvent(MenuManager.Menus cm, Player player, InventoryClickEvent e) {
		current_menu = cm;
		p = player;
		event = e;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return p;
	}

	public MenuManager.Menus getCurrent_menu() {
		return current_menu;
	}

	public InventoryClickEvent getEvent() {
		return event;
	}
}