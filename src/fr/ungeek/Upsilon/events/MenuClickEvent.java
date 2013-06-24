package fr.ungeek.Upsilon.events;

import fr.ungeek.Upsilon.MenuManager;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;

public class MenuClickEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private MenuManager.Menus current_menu;
    private InventoryClickEvent event;

    public MenuClickEvent(MenuManager.Menus cm, InventoryClickEvent e) {
        super(true);
        current_menu = cm;
        event = e;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public MenuManager.Menus getCurrent_menu() {
        return current_menu;
    }

    public InventoryClickEvent getEvent() {
        return event;
    }
}