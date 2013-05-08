package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CloseMenuEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	private Main.Menus closed_menu;
	private Player p;

	public CloseMenuEvent(Main.Menus cm, Player player) {
		closed_menu = cm;
		p = player;
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

	public Main.Menus getClosed_menu() {
		return closed_menu;
	}
}