package fr.ungeek.Upsilon;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 5/13/13
 * Time: 9:06 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Commandes {
	Main main;

	public Commandes(Main main) {
		this.main = main;
	}

	@CommandController.CommandHandler(name = "upsilon")
	public void onUpsilon(CommandSender cs, String[] args) {
		cs.sendMessage("Usage : /ups [events,open]");
	}

	@CommandController.SubCommandHandler(parent = "upsilon", name = "events", permission = "upsilon.admin.event")
	public void onUpsilonEvents(CommandSender cs, String[] args) {
		if (args.length != 3) {
			cs.sendMessage("Usage : /ups events <status> <event> <broadcast>");
			cs.sendMessage("Usage : /ups events <on,off> <nom> <on/off>");
			HashMap<String, Boolean> events = main.event_menu.getWarps();
			for (String n : events.keySet()) {
				cs.sendMessage(n + " : " + events.get(n).toString());
			}
		} else {
			Boolean enable = args[0].equalsIgnoreCase("on");
			String event = args[1];
			Boolean broadcast = args[2].equalsIgnoreCase("on");
			Boolean success = main.event_menu.changeState(event, enable);
			if (!success) {
				cs.sendMessage("Event inexistant, sale noob");
			} else {
				main.broadcastToAdmins(ChatColor.GRAY + "<" + cs.getName() + "> Event " + event + " mis " + (enable ? "on" : "off"));
				cs.sendMessage("État changé :)");
				if (broadcast) {
					if (enable) {
						Bukkit.broadcastMessage(main.getTAG() + ChatColor.DARK_GREEN + "Event " + event + " activé !");
					} else {
						Bukkit.broadcastMessage(main.getTAG() + ChatColor.DARK_RED + "Event " + event + " désactivé !");
					}
				}
			}
		}
	}

	@CommandController.SubCommandHandler(parent = "upsilon", name = "open", permission = "upsilon.admin.forceopen")
	public void onUpsilonForceOpen(CommandSender cs, String[] args) {
		if (args.length == 1) {
			cs.sendMessage("Usage /ups open <menu> [pseudo]");
			cs.sendMessage("Menus : " + StringUtils.join(MenuManager.Menus.values(), ", "));
			return;
		}
		MenuManager.Menus menu = null;
		Player p = null;
		if (MenuManager.Menus.contains(args[1].toUpperCase())) menu = MenuManager.Menus.valueOf(args[1].toUpperCase());
		if (args.length == 3) {
			p = Bukkit.getPlayer(args[2]);
		} else {
			if (cs instanceof Player) p = (Player) cs;
		}
		if (menu == null) {
			cs.sendMessage("Menu non trouvé");
			return;
		}
		if (p == null) {
			cs.sendMessage("Joueur non trouvé");
			return;
		}
		boolean usage = false;
		main.menu_manager.openInventory(p, menu);
		main.broadcastToAdmins(ChatColor.GRAY + "<" + cs.getName() + "> Menu " + menu + " ouvert pour " + p.getDisplayName());

	}
}
