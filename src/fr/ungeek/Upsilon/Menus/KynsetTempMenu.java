package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.ChangeMenuEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class KynsetTempMenu implements Listener {
	Main m;
	MenuManager MM;
	List<ItemStack> items = new ArrayList<ItemStack>();

	public KynsetTempMenu(Main main, MenuManager MM) {
		m = main;
		this.MM = MM;

		items.add(nameItem("GTA III", "ufpop5"));
		items.add(nameItem("Civilization III Complete Edition", "9usb98"));
		items.add(nameItem("Ace of Spades", "3xt5ku"));
		items.add(nameItem("Mirror's Edge", "9fvt5b"));
		items.add(nameItem("The Binding of Isaac", "1e3993"));

		items.add(nameItem("Defense Grid", "sgn1xm"));
		items.add(nameItem("Defense Grid", "ffxubp"));

		items.add(nameItem("Dota 2", "lmwhs4"));
		items.add(nameItem("Dota 2", "gixmso"));
		items.add(nameItem("Dota 2", "e4ggq7"));
		items.add(nameItem("Dota 2", "yvcz8x"));
		items.add(nameItem("Dota 2", "pk9c84"));
		items.add(nameItem("Dota 2", "kj4f56"));
		items.add(nameItem("Dota 2", "5krg1c"));
		items.add(nameItem("Dota 2", "83n4qv"));
		items.add(nameItem("Dota 2", "fkul8n"));
		items.add(nameItem("Dota 2", "xiupmn"));

		items.add(nameItem("Clé Steam", "gcb0zq"));
		items.add(nameItem("Clé Steam", "jo5yb2"));
		items.add(nameItem("Clé Steam", "jga3cp"));

		items.add(nameItem("Code Minefight", "I7NTHN"));
		items.add(nameItem("Code Minefight", "T4GBMY"));
		items.add(nameItem("Code Minefight", "MSMCLE"));
		items.add(nameItem("Code Minefight", "E8OAOK"));
		items.add(nameItem("Code Minefight", "V5MF0U"));
		items.add(nameItem("Code Minefight", "8QI7L5"));
		items.add(nameItem("Code Minefight", "YPHG2E"));

	}

	public ItemStack nameItem(String name, String cle) {
		return m.nameItem(new ItemStack(Material.NETHER_STAR), name, ChatColor.RESET + "Vous avez gagné " + name, ChatColor.RESET + "Entrez le code " + ChatColor.DARK_GREEN + cle.toUpperCase() + ChatColor.RESET + " sur", ChatColor.DARK_AQUA + "http://minefight.fr/event.php" + ChatColor.RESET + "  pour recevoir le lot !");
	}

	public MenuManager.Menus getSelfMenuType() {
		return MenuManager.Menus.KYNSET;
	}

	@EventHandler
	public void onMenuOpen(ChangeMenuEvent e) {
		if (!e.getNew_menu().equals(getSelfMenuType())) return;
		MM.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
		Inventory inv = Bukkit.createInventory(null, 9 * (int) Math.ceil((double) items.size() / (double) 9), ChatColor.GOLD + "Kynset Hidden's Menu");
		inv.setContents(items.toArray(new ItemStack[0]));

		e.getPlayer().openInventory(inv);
	}
}
