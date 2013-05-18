package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.MenuManager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class MenuChangeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	boolean isAsync;
	private MenuManager.Menus new_menu;
	private HumanEntity p;

	public MenuChangeEvent(MenuManager.Menus nm, HumanEntity player) {

		super(true);
		new_menu = nm;
		p = player;
		isAsync = true;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public MenuManager.Menus getNew_menu() {
		return new_menu;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public HumanEntity getPlayer() {
		return p;
	}
}