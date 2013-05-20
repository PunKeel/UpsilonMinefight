package fr.ungeek.Upsilon;

import com.earth2me.essentials.Essentials;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import fr.ungeek.Upsilon.Games.RoiAuSommet;
import fr.ungeek.Upsilon.Menus.*;
import me.games647.scoreboardstats.ScoreboardStats;
import me.games647.scoreboardstats.api.pvpstats.Database;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

//import fr.ungeek.Upsilon.Menus.ShopMenu;

/**
 * User: PunKeel
 * Date: 5/3/13
 * Time: 8:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin {
	public Economy econ;
	public WorldGuardPlugin WG;
	public Essentials ess;
	public RegionManager RM;
	// Own classes
	public MenuManager menu_manager = new MenuManager(this);
	public MainMenu main_menu = new MainMenu(this, menu_manager);
	public EnchantMenu enchant_menu = new EnchantMenu(this, menu_manager);
	public TeleportationMenu teleportation_menu = new TeleportationMenu(this, menu_manager);
	public EventMenu event_menu = new EventMenu(this, menu_manager);
	public AnvilMenu anvil_menu = new AnvilMenu(this, menu_manager);
	public MoneyListener gimme_emerald = new MoneyListener(this);
	public RoiAuSommet roi = new RoiAuSommet(this);
	public Commandes commandes = new Commandes(this);
	public Arenas arenas;
	// Variables
	public String TAG;
	public SpawnManager SM;
	public PermissionManager PEX;
	public Gson gson = new Gson();

	public static <T> T getRandom(T[] array) {
		int rnd = new Random().nextInt(array.length);
		return array[rnd];
	}

	public Arenas getArenas() {
		return arenas;
	}

	public String getTAG() {
		return TAG;
	}

	public void onEnable() {
		SM = gson.fromJson(getConfig().getString("spawn_locations"), SpawnManager.class);
		TAG = ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Minefight" + ChatColor.DARK_GREEN + "] " + ChatColor.RESET;
		if (getConfig().contains("events"))
			event_menu.loadWarps();
		Bukkit.getPluginManager().registerEvents(gimme_emerald, this);
		Bukkit.getPluginManager().registerEvents(enchant_menu, this);
		Bukkit.getPluginManager().registerEvents(teleportation_menu, this);
		Bukkit.getPluginManager().registerEvents(main_menu, this);
		Bukkit.getPluginManager().registerEvents(event_menu, this);
		Bukkit.getPluginManager().registerEvents(anvil_menu, this);
		Bukkit.getPluginManager().registerEvents(menu_manager, this);
		Bukkit.getPluginManager().registerEvents(roi, this);
		enchant_menu.load_config();
		CommandController.registerCommands(this, commandes);
		getCommand("roi").setExecutor(roi);
		setupEconomy();
		setupWorldGuard();
		setupEssentials();
		setupScoreBoardStats();
		PEX = PermissionsEx.getPermissionManager();

		arenas = new Arenas(this);
		gimme_emerald.loadAmelioration();


	}

	public void teleportToWarp(String warp, HumanEntity p) {
		if (p == null) return;
		Location warp_loc;
		try {
			warp_loc = ess.getWarps().getWarp(warp);
		} catch (Exception e) {
			warp_loc = null;
		}
		if (warp_loc == null) {
			broadcastToAdmins(TAG + "Warp inexistant (" + warp + ")");
			if (p instanceof Player)
				((Player) p).sendMessage(TAG + "Une erreur est survenue");
			return;
		}
		p.teleport(warp_loc);
	}

	private void setupWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
			return;
		}
		WG = (WorldGuardPlugin) plugin;
		World world = Bukkit.getWorld("world");
		RM = WGBukkit.getRegionManager(world);
	}

	private void setupScoreBoardStats() {
		Plugin plugin = getServer().getPluginManager().getPlugin("ScoreboardStats");
		if (plugin == null || !(plugin instanceof ScoreboardStats)) {
			return;
		}
		ScoreboardStats SbS = (ScoreboardStats) plugin;
		Database.setDatabase(SbS.getDatabase());
	}

	private void setupEssentials() {

		Plugin plugin = getServer().getPluginManager().getPlugin("Essentials");
		if (plugin == null || !(plugin instanceof Essentials)) {
			return;
		}
		ess = (Essentials) plugin;
	}

	public void onDisable() {
		menu_manager.closeAll();
		getConfig().set("events", event_menu.getWarps());
		getConfig().set("level_max", enchant_menu.getLevel_max());
		getConfig().set("spawn_locations", gson.toJson(SM));
		saveConfig();
	}

	private boolean setupEconomy() {
		if (this.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
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

	public ItemStack nameItem(ItemStack i, String name, String lore1, String lore2) {
		return nameItem(i, name, lore1, lore2, null);
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

	public ItemStack nameItem(ItemStack i, String name, String lore1, String lore2, String lore3) {
		ItemMeta im = i.getItemMeta();
		if (name == null) name = "";
		if (lore1 == null) lore1 = "";
		if (lore2 == null) lore2 = "";
		if (lore3 == null) lore3 = "";

		if (name.isEmpty()) {
			im.setDisplayName("");
		} else {
			im.setDisplayName(ChatColor.MAGIC + "" + ChatColor.RESET + name);
		}
		if (!lore1.isEmpty()) {
			ArrayList<String> lore = new ArrayList<String>();

			lore.add(lore1);
			if (!lore2.isEmpty()) {
				lore.add(lore2);
			}
			if (!lore3.isEmpty()) {
				lore.add(lore3);
			}
			im.setLore(lore);
		}
		i.setItemMeta(im);

		return i;
	}

	public boolean isAdmin(Player p) {
		if (isAdmin(p.getName(), false)) return true;
		if (p.isOp()) return true;
		if (p.hasPermission("upsilon.admin")) return true;
		return false;
	}

	public boolean isVIP(Player p) {
		if (isAdmin(p)) return true;
		if (p.hasPermission("upsilon.VIP")) return true;
		return false;
	}

	public boolean isVIP(String name) {
		return isVIP(Bukkit.getPlayerExact(name));
	}

	public boolean isAdmin(String name, boolean checkPlayer) {
		if (name.equalsIgnoreCase("dleot")) return true;
		if (name.equalsIgnoreCase("console")) return true;
		if (name.equalsIgnoreCase("server")) return true;
		if (checkPlayer)
			return isAdmin(Bukkit.getPlayerExact(name));
		else
			return false;
	}

	public void broadcastToAdmins(Object o) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.hasPermission("upsilon.admin.see")) {
				p.sendMessage(o.toString());
			}
		}
	}

	public boolean canUse(Player p) {
		return true;
	}

	public Integer getTimestamp() {
		return (int) (System.nanoTime() / 1000000000);
	}
}
