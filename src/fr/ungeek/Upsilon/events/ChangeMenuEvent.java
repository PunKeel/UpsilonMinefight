package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChangeMenuEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Main.Menus new_menu;
	private Player p;

	public ChangeMenuEvent(Main.Menus nm, Player player) {
		new_menu = nm;
		p = player;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public Main.Menus getNew_menu() {
		return new_menu;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return p;
	}
}