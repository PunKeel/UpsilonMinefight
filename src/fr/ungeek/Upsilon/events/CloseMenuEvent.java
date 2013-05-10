package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.MenuManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CloseMenuEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private MenuManager.Menus closed_menu;
	private HumanEntity p;

	public CloseMenuEvent(MenuManager.Menus cm, HumanEntity player) {
		closed_menu = cm;
		p = player;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public HumanEntity getPlayer() {
		return p;
	}

	public MenuManager.Menus getClosed_menu() {
		return closed_menu;
	}
}