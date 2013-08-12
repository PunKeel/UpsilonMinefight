package fr.ungeek.NonPremiumLogin;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.Kit;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.util.*;

import static fr.ungeek.NonPremiumLogin.CommandController.CommandHandler;

/**
 * User: PunKeel
 * Date: 6/15/13
 * Time: 4:45 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin implements Listener {
    AccountManager AM = new AccountManager(this);
    HashMap<String, Integer> offline = new HashMap<>();
    HashMap<String, String> online = new HashMap<>();
    boolean cracks_allowed = true;
    Set<String> newbies = new HashSet<>();
    Set<String> cracks = new HashSet<>();
    Premium P;
    private IEssentials ess;

    public static String getTAG() {
        return ChatColor.BLUE + "[" + ChatColor.WHITE + "Minefight" + ChatColor.BLUE + "] " + ChatColor.RESET;
    }

    public static <T extends Plugin> T getPlugin(String name, Class<T> classe) throws Exception {
        if (!Bukkit.getPluginManager().isPluginEnabled(name)) {
            throw new Exception("Plugin " + name + " is not loaded");
        }
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        if (plugin == null || !(classe.isAssignableFrom(plugin.getClass()))) {
            throw new Exception("Plugin" + name + " didn't return " + classe.getCanonicalName());
        }
        return classe.cast(plugin);
    }

    private boolean isPremium(String name) {
        return P.isPremium(name);
    }

    public void onEnable() {
        P = new Premium(this);
        P.loadPremiums();
        getServer().getPluginManager().registerEvents(this, this);
        CommandController.registerCommands(this, this);
        AM.loadAccounts();
        try {
            ess = getPlugin("Essentials", Essentials.class);
        } catch (Exception e) {
            e.printStackTrace();
            cracks_allowed = false;
        }
    }

    public void onDisable() {
        AM.saveAccounts();
        P.savePremiums();
        Player p;
        for (String name : offline.keySet()) {
            p = Bukkit.getPlayer(name);
            if (p == null) continue;
            if (!p.isOnline()) continue;
            p.kickPlayer(ChatColor.DARK_RED + "Reload alors que tu n'étais pas connecté");
        }
    }

    public boolean isLoggedIn(Player p) {
        return !offline.containsKey(p.getName().toLowerCase());
    }

    public void freezePlayer(Player p) {
        p.setWalkSpeed(0.0F);
        p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 10000, 128));
        offline.put(p.getName().toLowerCase(), 0);
    }

    public void unfreezePlayer(Player p) {
        offline.remove(p.getName().toLowerCase());
        p.setWalkSpeed(0.2F);
        p.removePotionEffect(PotionEffectType.JUMP);
        online.put(p.getName().toLowerCase(), p.getAddress().getAddress().getHostAddress());
    }

    /*@EventHandler()
    public void onPreLogin(PlayerHandshakeEvent e) {
        if (cracks_allowed)
            if (!P.isPremium(e.getPlayerName()))
                e.setLoginKey("-");
    }  */

   /* @EventHandler()
    public void onPreLogin(AsyncPlayerPreLoginEvent e) {
        boolean premium = !e.getLoginResult().equals(AsyncPlayerPreLoginEvent.Result.KICK_VERIFY);
        if (!premium) cracks.add(e.getName());
        e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
        if (e.getAddress().isLoopbackAddress()) return;
        String ip = e.getAddress().getHostAddress();
        for (Player x : Bukkit.getOnlinePlayers()) {
            if (x.getName().equalsIgnoreCase(e.getName()) && !isLoggedIn(x)) {
                e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.DARK_RED + "Tu es déjà en ligne");
                return;
            }

            if (x.getAddress().getAddress().getHostAddress().equals(ip)) {
                if (!(isPremium(x.getName()) && premium)) {
                    e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.DARK_RED + "Double compte interdit");
                    return;
                }
            }
        }
    }  */

    @EventHandler()
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (isLoggedIn(p)) {
            e.setQuitMessage(ChatColor.RED + "- " + ChatColor.RESET + p.getDisplayName());
        }
    }

    @EventHandler()
    public void onKick(PlayerKickEvent e) {
        Player p = e.getPlayer();
        if (isLoggedIn(p)) {
            e.setLeaveMessage(ChatColor.RED + "- " + ChatColor.RESET + p.getDisplayName());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent e) throws Exception {
        Player p = e.getPlayer();
        if (!p.hasPlayedBefore()) {
            newbies.add(p.getName());
        }
        PermissionUser u = PermissionsEx.getUser(p);
        String ip = p.getAddress().getAddress().getHostAddress();
        if (isPremium(p.getName())) {
            if (u.inGroup("cracked"))
                u.removeGroup("cracked");
            e.setJoinMessage(ChatColor.GREEN + "+ " + ChatColor.RESET + p.getDisplayName());
            if (!p.hasPlayedBefore()) {
                e.setJoinMessage(ChatColor.LIGHT_PURPLE + p.getName() + " est nouveau/nouvelle sur Minefight !");
                final Map<String, Object> kit = ess.getSettings().getKit("newbie");
                final List<String> items = Kit.getItems(ess, ess.getUser(p), kit);
                Kit.expandItems(ess, ess.getUser(p), items);
            }

        } else {
            u.addGroup("cracked");
            if (online.containsKey(p.getName().toLowerCase())) {
                if (online.get(p.getName().toLowerCase()).equals(ip)) {
                    unfreezePlayer(p);
                    e.setJoinMessage(ChatColor.GREEN + "+ " + ChatColor.GRAY + p.getDisplayName());
                    return;
                }
            }
            e.setJoinMessage("");
            freezePlayer(p);
            if (AM.isRegistered(p.getName())) {
                p.sendMessage(Main.getTAG() + "Tu dois te connecter");
                p.sendMessage("/l <mot de passe>");
            } else {
                p.sendMessage(Main.getTAG() + "Tu n'es pas inscrit.");
                p.sendMessage("/register <mot de passe>");
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) return;
        if (e.getEntity() instanceof Player) {
            Player d = (Player) e.getEntity();
            if (newbies.contains(d.getName())) {
                e.setDamage((int) (e.getDamage() / 1.25));
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!isLoggedIn(p)) {
            e.setCancelled(true);
            p.sendMessage(Main.getTAG() + "Tu dois te connecter pour faire ça");
            if (AM.isRegistered(p.getName()))
                p.sendMessage("/l <mot de passe>");
            else
                p.sendMessage("/register <mot de passe>");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (!isLoggedIn(p)) {
            e.setCancelled(true);
            p.sendMessage(Main.getTAG() + "Tu dois te connecter pour faire ça");
            if (AM.isRegistered(p.getName()))
                p.sendMessage("/l <mot de passe>");
            else
                p.sendMessage("/register <mot de passe>");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void on(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        if (!isLoggedIn(p)) {
            e.setCancelled(true);
            p.sendMessage(Main.getTAG() + "Tu dois te connecter pour faire ça");
            if (AM.isRegistered(p.getName()))
                p.sendMessage("/l <mot de passe>");
            else
                p.sendMessage("/register <mot de passe>");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void on(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (!isLoggedIn(p)) {
            e.setCancelled(true);
            p.sendMessage(Main.getTAG() + "Tu dois te connecter pour faire ça");
            if (AM.isRegistered(p.getName()))
                p.sendMessage("/l <mot de passe>");
            else
                p.sendMessage("/register <mot de passe>");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String args[] = e.getMessage().split(" ");
        Set<String> allowed = new HashSet<>();
        allowed.add("/login");
        allowed.add("/l");
        allowed.add("/register");
        allowed.add("/changepwd");
        allowed.add("/helpop");
        if (!allowed.contains(args[0].toLowerCase())) {
            if (!isLoggedIn(p)) {
                e.setCancelled(true);
                p.sendMessage(Main.getTAG() + "Tu dois te connecter pour faire ça");
                if (AM.isRegistered(p.getName()))
                    p.sendMessage("/l <mot de passe>");
                else
                    p.sendMessage("/register <mot de passe>");
            }
        }
    }

    @CommandHandler(name = "login")
    public void onLogin(Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage(Main.getTAG() + "Usage /l <password>");
            return;
        }
        if (isLoggedIn(p)) return;
        boolean success = AM.checkPassword(p.getName(), args[0]);
        if (success) {
            p.sendMessage(getTAG() + ChatColor.GREEN + "Tu es connecté !");
            unfreezePlayer(p);
            Bukkit.broadcastMessage(ChatColor.GREEN + "+ " + ChatColor.GRAY + p.getDisplayName());
        } else {
            p.sendMessage(Main.getTAG() + ChatColor.RED + "Mot de passe incorrect");
        }
    }

    @CommandHandler(name = "register")
    public void onRegister(Player p, String[] args) {
        if (isLoggedIn(p)) return;
        if (args.length != 1) {
            p.sendMessage(Main.getTAG() + "Usage /register <password>");
            return;
        }
        if (AM.isRegistered(p.getName())) {
            p.sendMessage(Main.getTAG() + "Tu es déjà inscrit, fais " + ChatColor.DARK_GREEN + "/changepwd <nouveau>" + ChatColor.RESET + " pour changer de mot de passe");
            return;
        }
        AM.setPassword(p.getName(), args[0]);
        Bukkit.broadcastMessage(ChatColor.GREEN + "+ " + ChatColor.GRAY + p.getDisplayName());
        p.sendMessage(Main.getTAG() + "Tu es bien inscrit et connecté.");
        unfreezePlayer(p);
    }

    @CommandHandler(name = "changepwd")
    public void onChangePWD(Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage(Main.getTAG() + "Usage /changepwd <password>");
            return;
        }
        if (!isLoggedIn(p)) {
            p.sendMessage(Main.getTAG() + "Tu n'es pas connecté, fais " + ChatColor.DARK_GREEN + "/l <password>" + ChatColor.RESET + " pour te connecter");
            return;
        }
        AM.setPassword(p.getName(), args[0]);
        p.sendMessage(Main.getTAG() + "Mot de passe changé.");
    }

    @CommandHandler(name = "changepass", permission = "upsilon.admin.changepass")
    public void onChangePass(CommandSender p, String[] args) {
        if (args.length != 2) {
            p.sendMessage(Main.getTAG() + "Usage /changepass <compte> <password>");
            return;
        }
        AM.setPassword(args[0], args[1]);
        p.sendMessage(Main.getTAG() + "Mot de passe changé pour " + args[0]);
    }

    @CommandHandler(name = "togglecrack", permission = "upsilon.admin.togglecrack")
    public void onToggleCrack(CommandSender p, String[] args) {
        if (args.length != 0) {
            p.sendMessage(Main.getTAG() + "Usage /togglecrack");
            return;
        }
        cracks_allowed = !cracks_allowed;
        if (cracks_allowed) {
            broadcastToAdmins(Main.getTAG() + "Cracks autorisés");
        } else {
            broadcastToAdmins(Main.getTAG() + "Cracks interdits");
        }
    }

    public void broadcastToAdmins(String o) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("upsilon.admin.see")) {
                p.sendMessage(o);
            }
        }
    }
}
