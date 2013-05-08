package fr.ungeek.Upsilon;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * User: PunKeel
 * Date: 5/3/13
 * Time: 8:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin implements Listener {
	public Economy econ;
	HashMap<String, Menus> current_menu = new HashMap<String, Menus>();
	EventMenu EM = new EventMenu(this);
	ShopMenu SM = new ShopMenu(this);
	TeleportationMenu TM = new TeleportationMenu(this);
	MenuInventory MI = new MenuInventory(this);

	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(this, this);

		//Bukkit.getPluginManager().registerEvents(EM, this);
		Bukkit.getPluginManager().registerEvents(TM, this);

		//Bukkit.getPluginManager().registerEvents(SM, this);

		Bukkit.getPluginManager().registerEvents(MI, this);
		setupEconomy();
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

	@EventHandler()
	public void onJoin(PlayerJoinEvent e) {
		final Player p = e.getPlayer();
		String today = getDate();
		String yesterday = getDate(-1);
		final String message;
		if (p.hasMetadata("ups_lastjoin")) {
			List<MetadataValue> ups_lastjoin = p.getMetadata("ups_lastjoin");
			String LastJoin = "";
			if (ups_lastjoin.size() != 0) {
				LastJoin = p.getMetadata("ups_lastjoin").get(0).asString();
			}
			if (!LastJoin.equals(today)) {
				int jours = 1;
				p.setMetadata("ups_lastjoin", new FixedMetadataValue(this, today));
				if (LastJoin.equals(yesterday)) {
					// consecutif
					jours = p.getMetadata("ups_follow").get(0).asInt() + 1;
					int gain = (jours > 5) ? 50 : jours * 10;
					p.setMetadata("ups_follow", new FixedMetadataValue(this, jours));
					message = (ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Minefight" + ChatColor.DARK_GREEN + "] " + ChatColor.WHITE + "Tu as reçu " + gain + " ƒ pour tes " + jours + " jours de présence à la suite !");
					econ.depositPlayer(p.getName(), gain);
				} else {
					// pas consecutif
					p.setMetadata("ups_follow", new FixedMetadataValue(this, jours));
					message = (ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Minefight" + ChatColor.DARK_GREEN + "] " + ChatColor.WHITE + "Tu as reçu 10 ƒ pour ton premier jour de présence consécutif!");
					econ.depositPlayer(p.getName(), 10);
				}
				if (!message.isEmpty()) {
					Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
						public void run() {
							p.sendMessage(message);
						}
					}, 20);
				}
			}

		} else {
			p.setMetadata("ups_lastjoin", new FixedMetadataValue(this, today));
			p.setMetadata("ups_follow", new FixedMetadataValue(this, 1));
		}
	}

	ItemStack nameItem(ItemStack i, String name, String lore1) {
		return nameItem(i, name, lore1, null);
	}

	ItemStack nameItem(ItemStack i, String name) {
		return nameItem(i, name, null);
	}

	ItemStack nameItem(ItemStack i) {
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

	public enum Menus {MAIN, TELEPORTATION, SHOP, EVENTS}

}
