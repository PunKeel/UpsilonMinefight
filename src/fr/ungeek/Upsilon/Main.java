package fr.ungeek.Upsilon;

import com.earth2me.essentials.Essentials;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import fr.ungeek.Upsilon.Games.RoiAuSommet;
import fr.ungeek.Upsilon.Games.Spleef;
import fr.ungeek.Upsilon.Menus.EnchantMenu;
import fr.ungeek.Upsilon.Menus.EventMenu;
import fr.ungeek.Upsilon.Menus.MainMenu;
import fr.ungeek.Upsilon.Menus.TeleportationMenu;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
//import fr.ungeek.Upsilon.DB.Suggestion;

//import fr.ungeek.Upsilon.Menus.ShopMenu;

/**
 * User: PunKeel
 * Date: 5/3/13
 * Time: 8:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin {
    public static String WORLDNAME = "world";
    public Economy econ;
    public WorldGuardPlugin WG;
    public WorldEditPlugin WE;
    public Essentials ess;
    public RegionManager RM;
    // Own classes
    public MenuManager menu_manager = new MenuManager(this);
    public MainMenu main_menu = new MainMenu(this, menu_manager);
    public EnchantMenu enchant_menu = new EnchantMenu(this, menu_manager);
    public TeleportationMenu teleportation_menu = new TeleportationMenu(this, menu_manager);
    public EventMenu event_menu = new EventMenu(this, menu_manager);
    public MoneyListener gimme_emerald = new MoneyListener(this);
    public RoiAuSommet roi = new RoiAuSommet(this);
    public Commandes commandes = new Commandes(this);
    public Chronos chrono = new Chronos(this);
    public InfiniDisp infinidisp = new InfiniDisp(this);
    public Arenas arenas = new Arenas(this);
    public Spleef spleef = new Spleef(this);
    // Variables
    public SpawnManager SM;
    public PermissionManager PEX;
    public Gson gson = new Gson();
    public HashMap<String, VoteKickHolder> votekick = new HashMap<>();
    public FriendManager FM = new FriendManager(this);
    public AntiCheat AC;
    //public Logger log = Logger.getLogger(this.getClass().getName(), true);
    private Logger CLogger;
    private long mainThreadName;
    private ConfigManager CM = new ConfigManager(this);
    private SimpleConfig globalConfig, locationsConfig, amisConfig;
    private WGCustomFlagsPlugin WGCF;
    private int bCasterThread = 0;

    public static <T> T getRandom(T[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    public static Main getMain() {
        try {
            return getPlugin("UpsilonProject", Main.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void alert(Player p, boolean admin) {
        if (admin) {
            p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 50.0F, 5.0F);
        } else {
            p.playSound(p.getLocation(), Sound.NOTE_PIANO, 50.0F, 5.0F);
            p.playSound(p.getLocation(), Sound.NOTE_STICKS, 50.0F, 15.0F);
        }
    }

    public static String getTAG() {
        return ChatColor.DARK_GREEN + "[" + ChatColor.WHITE + "Minefight" + ChatColor.DARK_GREEN + "] " + ChatColor.RESET;
    }

    public static Integer getTimestamp() {
        return (int) (System.nanoTime() / 1000000000);
    }

    public static void resetPlayer(HumanEntity p) {
        p.setHealth(20);
        p.setFireTicks(-20);
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
    }

    public static void resetPlayer(HumanEntity p, GameMode GM) {
        p.setHealth(20);
        p.setFireTicks(-20);
        for (PotionEffect effect : p.getActivePotionEffects())
            p.removePotionEffect(effect.getType());
        if (!p.getGameMode().equals(GM)) p.setGameMode(GM);
    }

    public static void print(String o) {
        System.out.println(o);
    }

    public static <T extends Plugin> T getPlugin(String name, Class<T> classe) throws UnknownPluginException {
        if (!Bukkit.getPluginManager().isPluginEnabled(name)) {
            throw new UnknownPluginException("Plugin " + name + " is not loaded");
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null || !(classe.isAssignableFrom(plugin.getClass()))) {
            throw new UnknownPluginException("Plugin" + name + " didn't return " + classe.getCanonicalName());
        }
        return classe.cast(plugin);
    }

    public Arenas getArenas() {
        return arenas;
    }

    @Override()
    public void onEnable() {
        mainThreadName = Thread.currentThread().getId();
        PEX = PermissionsEx.getPermissionManager();
        if (PEX == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        Bukkit.getPluginManager().registerEvents(gimme_emerald, this);
        Bukkit.getPluginManager().registerEvents(chrono, this);
        Bukkit.getPluginManager().registerEvents(enchant_menu, this);
        Bukkit.getPluginManager().registerEvents(teleportation_menu, this);
        Bukkit.getPluginManager().registerEvents(main_menu, this);
        Bukkit.getPluginManager().registerEvents(event_menu, this);
        Bukkit.getPluginManager().registerEvents(menu_manager, this);
        Bukkit.getPluginManager().registerEvents(spleef, this);
        Bukkit.getPluginManager().registerEvents(roi, this);
        Bukkit.getPluginManager().registerEvents(infinidisp, this);
        CommandController.registerCommands(this, commandes);
        CommandController.registerCommands(this, chrono);
        CommandController.registerCommands(this, infinidisp);
        CommandController.registerCommands(this, spleef);
        getCommand("roi").setExecutor(roi);
        setupDependencies();
        globalConfig = CM.getNewConfig("config.yml");
        locationsConfig = CM.getNewConfig("locations.yml");
        amisConfig = CM.getNewConfig("amis.yml");
        ConfigReload();
        arenas.init();
        menu_manager.init();
        Handler handler = null;
        try {
            handler = new FileHandler(getDataFolder() + "/upsilon.log", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SimpleFormatter formatter = new SimpleFormatter();
        assert handler != null;
        handler.setFormatter(formatter);
        CLogger = Logger.getLogger(getClass().getName());
        CLogger.addHandler(handler);
        AC = new AntiCheat(this);
        getServer().getPluginManager().registerEvents(AC, this);

    }

    public void setupDependencies() {
        try {
            WE = getPlugin("WorldEdit", WorldEditPlugin.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            WG = getPlugin("WorldGuard", WorldGuardPlugin.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        World world = Bukkit.getWorld(WORLDNAME);
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doMobLoot", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("mobGriefing", "false");
        RM = WGBukkit.getRegionManager(world);

        try {
            WGCF = getPlugin("WGCustomFlags", WGCustomFlagsPlugin.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            ess = getPlugin("Essentials", Essentials.class);
        } catch (UnknownPluginException e) {
            e.printStackTrace();
        }
        setupEconomy();
    }

    public void teleportToWarp(String warp, HumanEntity p) {
        if (p == null) return;
        if (ess.getUser(p).isJailed()) {
            ((Player) p).sendMessage(getTAG() + ChatColor.DARK_RED + "Tu es jail !");
        }
        Location warp_loc = getWarp(warp);
        if (warp_loc == null) {
            broadcastToAdmins(getTAG() + "Warp inexistant (" + warp + ")");
            if (p instanceof Player)
                ((Player) p).sendMessage(getTAG() + "Une erreur est survenue");
            return;
        }
        p.teleport(warp_loc);
    }

    public void onDisable() {
        menu_manager.closeAll();
        ConfigSave();
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
            ArrayList<String> lore = new ArrayList<>();

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
        return p.hasPermission("upsilon.admin");
    }

    public boolean isVIP(Player p) {
        if (isAdmin(p)) return true;
        return p.hasPermission("upsilon.VIP");
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

    public boolean canUse(Player p) {
        return true;
    }

    public void broadcastToAdmins(String o, boolean b) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("upsilon.admin.see")) {
                p.sendMessage(o);
                alert(p, true);
            }
        }
    }

    public void broadcastToAdmins(String o) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("upsilon.admin.see")) {
                p.sendMessage(o);
            }
        }
    }

    public long getMainThreadName() {
        return mainThreadName;
    }

    public void ConfigSave() {
        globalConfig.set("events", event_menu.getWarps(), "- Menu events");
        globalConfig.set("level_max", enchant_menu.getLevel_max(), "- Menu maxi enchantement VIP");
        locationsConfig.set("spawn_locations", gson.toJson(SM), "- Coords respawns par region");
        locationsConfig.set("infini_locations", gson.toJson(infinidisp.coordonnees), "- Coords infinidispensers");
        amisConfig.set("friends", gson.toJson(FM.amis));
        amisConfig.saveConfig();
        globalConfig.saveConfig();
        locationsConfig.saveConfig();
    }

    public void ConfigReload() {

        locationsConfig.reloadConfig();
        globalConfig.reloadConfig();

        if (globalConfig.contains("events"))
            event_menu.loadWarps();

        gimme_emerald.loadAmelioration();

        enchant_menu.load_config();
        /*try {
            FileHandler f = new FileHandler(getDataFolder().getAbsolutePath() + "/log.txt", true);
			log.addHandler(f);
		} catch (IOException e) {
			e.printStackTrace();
		}*/


        SM = gson.fromJson(locationsConfig.getString("spawn_locations"), SpawnManager.class);
        if (SM == null)
            SM = new SpawnManager();
        infinidisp.coordonnees = gson.fromJson(locationsConfig.getString("infini_locations"), (new HashSet<String>()).getClass());
        if (infinidisp.coordonnees == null)
            infinidisp.coordonnees = new HashSet<>();
        if (amisConfig.contains("friends"))
            FM.amis = gson.fromJson(amisConfig.getString("friends"), (new HashSet<String>()).getClass());
        final List bCastMessages = globalConfig.getList("broadcast-messages");
        if (bCasterThread != 0)
            Bukkit.getScheduler().cancelTask(bCasterThread);
        bCasterThread = Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {
            int i = 0;
            Object[] messages = bCastMessages.toArray();

            @Override
            public void run() {
                Bukkit.broadcastMessage("[" + ChatColor.BLUE + "Annonce" + ChatColor.RESET + "] " + ChatColor.BLUE + ChatColor.translateAlternateColorCodes('&', messages[i].toString()));
                i = i + 1;
                if (i >= messages.length) i = 0;
            }
        }, 20, 20 * 60 * 3);

    }

    public WGCustomFlagsPlugin getWGCF() {
        return WGCF;
    }

    public Location getWarp(String warp) {
        try {
            return ess.getWarps().getWarp(warp);
        } catch (Exception e) {
            return null;
        }
    }

    public Logger getCLogger() {
        Player p = Bukkit.getPlayer("DleoT");
        Location shootLocation = p.getLocation();
        Vector directionVector = shootLocation.getDirection().normalize();
        double startShift = 2;
        Vector shootShiftVector = new Vector(directionVector.getX() * startShift, directionVector.getY() * startShift, directionVector.getZ() * startShift);
        shootLocation = shootLocation.add(shootShiftVector.getX(), shootShiftVector.getY(), shootShiftVector.getZ());
        Fireball fireballl = shootLocation.getWorld().spawn(shootLocation, Fireball.class);
        fireballl.setVelocity(directionVector.multiply(2));
        fireballl.setIsIncendiary(false);// Remove fire
        fireballl.setShooter(p.getPlayer());
        return CLogger;

    }

    static class UnknownPluginException extends Exception {
        UnknownPluginException(String message) {
            super(message);
        }
    }
}
