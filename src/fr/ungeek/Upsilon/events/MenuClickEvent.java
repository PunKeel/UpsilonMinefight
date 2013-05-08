package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuClickEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Main.Menus current_menu;
	private Player p;
	private InventoryClickEvent event;

	public MenuClickEvent(Main.Menus cm, Player player, InventoryClickEvent e) {
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

	public Main.Menus getCurrent_menu() {
		return current_menu;
	}

	public InventoryClickEvent getEvent() {
		return event;
	}
}