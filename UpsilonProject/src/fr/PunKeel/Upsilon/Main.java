package fr.PunKeel.Upsilon;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import com.google.common.base.Joiner;
import com.mewin.WGCustomFlags.WGCustomFlagsPlugin;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import fr.PunKeel.Upsilon.Games.RoiAuSommet;
import fr.PunKeel.Upsilon.Games.Spleef;
import fr.PunKeel.Upsilon.Menus.EnchantMenu;
import fr.PunKeel.Upsilon.Menus.EventMenu;
import fr.PunKeel.Upsilon.Menus.MainMenu;
import fr.PunKeel.Upsilon.Menus.TeleportationMenu;
import org.bukkit.*;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * User: PunKeel
 * Date: 5/3/13
 * Time: 8:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin {
    static final StateFlag FLAG_ARENE = new StateFlag("arene", false);
    static final StateFlag FLAG_DIE_ON_LEAVE = new StateFlag("die_on_leave", false);
    static final StateFlag FLAG_EMERAUDE = new StateFlag("emeraude", true);
    public static String WORLDNAME = "world";
    public static String WORLDGAME = "world_void";
    public static Random rnd = new Random();
    public WorldEditPlugin WE;
    public Essentials ess;
    // Own classes
    public MenuManager menu_manager = new MenuManager(this);
    public EventMenu event_menu = new EventMenu(this, menu_manager);
    // Variables
    public SpawnManager SM;
    public PermissionManager PEX;
    public AntiCheat AC;
    public HashMap<String, VoteKickHolder> votekick = new HashMap<>();
    Boussole B;
    TeamManager TM;
    MoneyListener moneyListener = new MoneyListener(this);
    Halloween halloween = new Halloween(this);
    private MainMenu main_menu = new MainMenu(this, menu_manager);
    private EnchantMenu enchant_menu = new EnchantMenu(this, menu_manager);
    private TeleportationMenu teleportation_menu = new TeleportationMenu(this, menu_manager);
    private RoiAuSommet roi = new RoiAuSommet(this); // @TDOO: convert to use Command manager + special inventory
    private Commandes commandes = new Commandes(this);
    private Chronos chrono = new Chronos(this);
    private InfiniDisp infinidisp = new InfiniDisp(this);
    private Spleef spleef = new Spleef(this);
    private Gson gson = new GsonBuilder().registerTypeAdapter(SLocation.class,
            new SLocationSerialiser()).create();
    private Logger CLogger;
    private ConfigManager CM = new ConfigManager(this);
    private SimpleConfig globalConfig, locationsConfig, amisConfig;
    private WGCustomFlagsPlugin WGCF;
    private int bCasterThread = 0;
    private String log_date;
    private DeathManager DM = new DeathManager(this);

    public static <T> T getRandom(T[] array) {
        return array[rnd.nextInt(array.length)];
    }

    public static <A> A getRandom(Collection<A> c) {
        return new ArrayList<>(c).get(rnd.nextInt(c.size()));
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

    public static int getTimestamp() {
        return (int) (new GregorianCalendar().getTimeInMillis() / 1000);
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

    public static void print(String o) { // isn't this useless ?
        System.out.println(o);
    }

    private static <T extends Plugin> T getPlugin(String name, Class<T> classe) throws UnknownPluginException {
        if (!Bukkit.getPluginManager().isPluginEnabled(name)) {
            throw new UnknownPluginException("Plugin " + name + " is not loaded");
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null || !(classe.isAssignableFrom(plugin.getClass()))) {
            throw new UnknownPluginException("Plugin" + name + " didn't return " + classe.getCanonicalName());
        }
        return classe.cast(plugin);
    }

    public static int getRand(int min, int max) {
        if (min == max)
            return min;
        if (max < min) {
            // Swap variables ... without using a temp' one. <3
            min = min + max;
            max = min - max;
            min = min - max;
        }
        return min + (int) (Math.random() * ((max - min) + 1));

    }

    @Override()
    public void onEnable() {
        PEX = PermissionsEx.getPermissionManager();
        if (PEX == null) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        TM = new TeamManager(this);
        Bukkit.getPluginManager().registerEvents(DM, this);
        Bukkit.getPluginManager().registerEvents(moneyListener, this);
        Bukkit.getPluginManager().registerEvents(chrono, this);
        Bukkit.getPluginManager().registerEvents(enchant_menu, this);
        Bukkit.getPluginManager().registerEvents(teleportation_menu, this);
        Bukkit.getPluginManager().registerEvents(main_menu, this);
        Bukkit.getPluginManager().registerEvents(event_menu, this);
        Bukkit.getPluginManager().registerEvents(menu_manager, this);
        Bukkit.getPluginManager().registerEvents(spleef, this);
        Bukkit.getPluginManager().registerEvents(roi, this);
        Bukkit.getPluginManager().registerEvents(infinidisp, this);
        Bukkit.getPluginManager().registerEvents(TM, this);
        CommandController.registerCommands(this, commandes);
        CommandController.registerCommands(this, chrono);
        CommandController.registerCommands(this, infinidisp);
        CommandController.registerCommands(this, spleef);
        CommandController.registerCommands(this, halloween);
        getCommand("roi").setExecutor(roi);
        B = new Boussole(this);
        setupDependencies();

        getWGCF().addCustomFlag(FLAG_ARENE);
        getWGCF().addCustomFlag(FLAG_DIE_ON_LEAVE);
        getWGCF().addCustomFlag(FLAG_EMERAUDE);
        globalConfig = CM.getNewConfig("config.yml");
        locationsConfig = CM.getNewConfig("locations.yml");
        amisConfig = CM.getNewConfig("amis.yml");
        ConfigReload();
        AC = new AntiCheat(this);
        getServer().getPluginManager().registerEvents(AC, this);

        setupPayhour();
    }

    void addToBalance(String username, int amount) {
        try {
            com.earth2me.essentials.api.Economy.add(username, amount);
        } catch (UserDoesNotExistException | NoLoanPermittedException e) {
            e.printStackTrace();
        }
    }

    private void setupPayhour() {
        Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, new BukkitRunnable() {
            Set<String> players
                    ,
                    players_old;

            @Override
            public void run() {
                int gain;
                if (players != null) {
                    for (String k : players) {
                        Player p = Bukkit.getPlayerExact(k);
                        if (p == null || !p.isOnline()) continue;

                        gain = 10;
                        if (players_old != null)
                            if (players_old.contains(k))
                                gain += 5; // Si présent depuis looongtemps, +5
                        if (isVIP(k))
                            gain *= 10; // gain *10 si VIP :D

                        p.sendMessage(getTAG() + ChatColor.RED + "Payday !" + ChatColor.RESET + " Minefight t'offre " + gain + "ƒ pour ta présence ! :)");
                        addToBalance(k, gain);

                    }
                }
                players_old = players;
                players = ess.getUserMap().getAllUniqueUsers();


            }
        }, 10, 60 * 30 * 20); // 60 * 29 * 20 = 29 minutes
    }

    void setupDependencies() {
        try {
            WE = getPlugin("WorldEdit", WorldEditPlugin.class);
        } catch (Exception e) {
            e.printStackTrace();
        }

        World world = Bukkit.getWorld(WORLDNAME);
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doMobLoot", "false");
        world.setGameRuleValue("keepInventory", "true");
        world.setGameRuleValue("mobGriefing", "false");

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
        ConfigSave();
        menu_manager.closeAll();
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

    ItemStack nameItem(ItemStack i, String name, String lore1, String lore2, String lore3) {
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
        return isAdmin(p.getName(), false) || p.isOp() || p.hasPermission("upsilon.admin");
    }

    boolean isVIP(Player p) {
        return isAdmin(p) || p.hasPermission("upsilon.VIP");
    }

    public boolean isVIP(String name) {
        return isVIP(Bukkit.getPlayerExact(name));
    }

    boolean isAdmin(String name, boolean checkPlayer) {
        return name.equalsIgnoreCase("dleot") || name.equalsIgnoreCase("console") || name.equalsIgnoreCase("server") || checkPlayer && isAdmin(Bukkit.getPlayerExact(name));
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

    public void ConfigSave() {
        globalConfig.set("events", event_menu.getWarps(), "- Menu events");
        globalConfig.set("level_max", enchant_menu.getLevel_max(), "- Menu maxi enchantement VIP");
        locationsConfig.set("spawn_locations", gson.toJson(SM), "- Coords respawns par region");
        locationsConfig.set("infini_locations", gson.toJson(infinidisp.coordonnees), "- Coords infinidispensers");
        amisConfig.set("halloween", Joiner.on("|").join(halloween.locations));

        amisConfig.saveConfig();
        globalConfig.saveConfig();
        locationsConfig.saveConfig();
    }

    public void ConfigReload() {

        locationsConfig.reloadConfig();
        globalConfig.reloadConfig();

        if (globalConfig.contains("events"))
            event_menu.loadWarps();

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
        if (amisConfig.contains("halloween"))
            Collections.addAll(halloween.locations, amisConfig.getString("halloween").split("|"));
        if (halloween.locations == null)
            halloween.locations = new HashSet<>();
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
        String timestamp = new SimpleDateFormat("dd.MM.yyyy").format(new Date());

        if (CLogger == null || !log_date.equals(timestamp)) {
            log_date = timestamp;
            Handler handler;
            try {
                handler = new FileHandler(getDataFolder() + "/upsilon_" + timestamp + ".log", true);
            } catch (IOException e) {
                e.printStackTrace();
                handler = new ConsoleHandler();
            }
            CLogger = Logger.getLogger(this.getClass().getName());
            CLogger.setUseParentHandlers(false);

            LogFormatter formatter = new LogFormatter();
            handler.setFormatter(formatter);

            CLogger.addHandler(handler);
        }
        return CLogger;

    }

    static class UnknownPluginException extends Exception {
        UnknownPluginException(String message) {
            super(message);
        }
    }
}
