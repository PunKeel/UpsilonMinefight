package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.MenuChangeEvent;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftInventoryAnvil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class AnvilMenu implements Listener {
	Main m;
	MenuManager MM;

	public AnvilMenu(Main main, MenuManager MM) {
		m = main;
		this.MM = MM;
	}

	public MenuManager.Menus getSelfMenuType() {
		return MenuManager.Menus.ANVIL;
	}

	@EventHandler
	public void onMenuOpen(MenuChangeEvent e) {
		if (!e.getNew_menu().equals(getSelfMenuType())) return;
		MM.current_menu.remove(e.getPlayer().getName());
		e.getPlayer().openInventory(new CraftInventoryAnvil(null, null));

	}

}
