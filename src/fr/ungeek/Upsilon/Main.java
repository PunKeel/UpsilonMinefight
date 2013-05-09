package fr.ungeek.Upsilon;

//import fr.ungeek.Upsilon.Menus.EventMenu;

import fr.ungeek.Upsilon.Menus.EventMenu;
import fr.ungeek.Upsilon.Menus.MainMenu;
import fr.ungeek.Upsilon.Menus.TeleportationMenu;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

//import fr.ungeek.Upsilon.Menus.ShopMenu;

/**
 * User: PunKeel
 * Date: 5/3/13
 * Time: 8:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin {
	public Economy econ;
	MenuManager menu_manager = new MenuManager(this);
	MainMenu main_menu = new MainMenu(this, menu_manager);
	TeleportationMenu teleportation_menu = new TeleportationMenu(this, menu_manager);
	EventMenu event_menu = new EventMenu(this, menu_manager);
	DailyGift daily_gift = new DailyGift(this);

	public void onEnable() {
		if (getConfig().contains("events"))
			event_menu.loadWarps();
		Bukkit.getPluginManager().registerEvents(daily_gift, this);
		Bukkit.getPluginManager().registerEvents(teleportation_menu, this);
		Bukkit.getPluginManager().registerEvents(main_menu, this);
		Bukkit.getPluginManager().registerEvents(event_menu, this);
		Bukkit.getPluginManager().registerEvents(menu_manager, this);
		getCommand("upsilon").setExecutor(this);
		setupEconomy();
	}

	public void onDisable() {
		getConfig().set("events", event_menu.getWarps());
		saveConfig();
	}

	private boolean setupEconomy() {
		if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = (Economy) rsp.getProvider();
		return econ != null;
	}

	public boolean isToday(Integer day, Integer month, Integer year) {
		Calendar today = Calendar.getInstance();
		return (today.get(Calendar.DAY_OF_MONTH) == day && (today.get(Calendar.MONTH) + 1) == month && today.get(Calendar.YEAR) == year);
	}

	public void print(Object o) {
		System.out.println(o);
	}

	public boolean isBefore(Integer day, Integer month, Integer year) {
		Calendar today = Calendar.getInstance();
		if (today.get(Calendar.YEAR) > year) return true;
		if (today.get(Calendar.MONTH) > month) return true;
		if (today.get(Calendar.DAY_OF_MONTH) > day) return true;
		return false;
	}

	public String getDate(int diff) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, diff + 123);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(cal.getTime());
	}

	public String getDate() {
		return getDate(0);
	}

	public ItemStack nameItem(ItemStack i, String name, String lore1) {
		return nameItem(i, name, lore1, null);
	}

	public ItemStack nameItem(ItemStack i, String name) {
		return nameItem(i, name, null);
	}

	public ItemStack nameItem(ItemStack i) {
		return nameItem(i, null);
	}

	ItemStack nameItem(ItemStack i, String name, String lore1, String lore2) {
		ItemMeta im = i.getItemMeta();
		if (name == null) name = "";
		if (lore1 == null) lore1 = "";
		if (lore2 == null) lore2 = "";

		if (name.isEmpty()) {
			im.setDisplayName("");
		} else {
			im.setDisplayName(name);
		}
		if (!lore1.isEmpty()) {
			ArrayList<String> lore = new ArrayList<String>();

			lore.add(lore1);
			if (!lore2.isEmpty()) {
				lore.add(lore2);
			}
			im.setLore(lore);
		}
		i.setItemMeta(im);
		return i;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		Player p = null;
		Boolean usage = false;
		if (sender instanceof Player) {
			p = (Player) sender;
		}
		if (args.length == 0) {
			sender.sendMessage("Usage : /" + label + " [events,open]");
			return true;
		}
		if (args[0].equalsIgnoreCase("events")) {
			if (args.length != 3) {
				sender.sendMessage("Usage : /" + label + " events <on,off> <event>");
				HashMap<String, Boolean> events = event_menu.getWarps();
				for (String n : events.keySet()) {
					sender.sendMessage(n + " : " + events.get(n).toString());
				}
			} else {
				Boolean enable = (args[1].equalsIgnoreCase("off")) ? false : true;
				String event = args[2];
				Boolean success = event_menu.changeState(event, enable);
				if (!success) {
					sender.sendMessage("Event inexistant, sale noob");
				} else {
					sender.sendMessage("État changé :)");
				}
			}
		} else if (args[0].equalsIgnoreCase("open")) {
			if (args.length == 2) {
				Player cible = Bukkit.getPlayer(args[1]);
				if (cible != null) {
					menu_manager.openInventory(cible, MenuManager.Menus.MAIN);
				} else if (MenuManager.Menus.contains(args[1].toUpperCase())) {
					if (p != null) {
						menu_manager.openInventory(p, MenuManager.Menus.valueOf(args[1].toUpperCase()));
					} else {
						sender.sendMessage("Tu dois être un joueur pour faire ça.");
					}
				} else {
					usage = true;
				}
			} else if (args.length == 3) {
				Player cible = Bukkit.getPlayer(args[2]);
				if (cible == null) {
					sender.sendMessage("Joueur introuvable");
				} else {
					if (!MenuManager.Menus.contains(args[1].toUpperCase())) {
						sender.sendMessage("Menu introuvable");
						usage = true;
					} else {
						menu_manager.openInventory(cible, MenuManager.Menus.valueOf(args[1].toUpperCase()));
						sender.sendMessage("Menu " + args[1].toUpperCase() + "ouvert pour " + cible.getDisplayName());
					}
				}
			} else {
				usage = true;
			}
			if (usage) {
				sender.sendMessage("Usage /" + label + " open <menu> [pseudo]");
				sender.sendMessage("Menus : " + StringUtils.join(MenuManager.Menus.values(), ", "));
			}

		}
		return true;
	}
}
