package fr.PunKeel.Upsilon;

import com.earth2me.essentials.api.Economy;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
import me.confuser.barapi.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * User: PunKeel
 * Date: 9/29/13
 * Time: 8:49 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class TeamManager implements Listener {
    HashSet<String> blacklist = new HashSet<>();
    Scoreboard SB;
    HashMap<String, Float> scores = new HashMap<>();
    Main main;
    HashMap<String, String> team_joueur = new HashMap<>();

    public TeamManager(Main m) {
        main = m;
        blacklist.add("localhost");
        blacklist.add("minefight");
        blacklist.add("play");
        blacklist.add("www");
        SB = Bukkit.getScoreboardManager().getNewScoreboard();

    }

    void broadcastTeamBarMessage(Team team, String message) {
        for (OfflinePlayer p : team.getPlayers())
            if (p.isOnline())
                BarAPI.setMessage((Player) p, "[" + ChatColor.GREEN + team.getDisplayName() + ChatColor.RESET + "] " + message);
    }

    void broadcastTeamBarHealth(Team team, float health) {
        for (OfflinePlayer p : team.getPlayers())
            if (p.isOnline())
                BarAPI.setHealth((Player) p, health);
    }

    void quit(Player p) {
        Team t = SB.getPlayerTeam(p);
        if (t == null)
            return;
        t.removePlayer(p);
        if (t.getPlayers().size() == 0)
            t.unregister();
        else
            broadcastTeamBarMessage(t, p.getDisplayName() + ChatColor.GOLD + " vous quitte");

    }

    private void broadcastTeamMessage(Team team, String message) {
        for (OfflinePlayer p : team.getPlayers())
            if (p.isOnline())
                ((Player) p).sendMessage(message);
    }

    private void shareMoney(Team team, int amount) {
        for (OfflinePlayer p : team.getPlayers())
            if (p.isOnline())
                try {
                    Economy.add(p.getName(), BigDecimal.valueOf(amount));
                } catch (UserDoesNotExistException | NoLoanPermittedException ignored) {
                }

    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(PlayerLoginEvent e) {
        if (!e.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) return;
        Player p = e.getPlayer();
        if (team_joueur.containsKey(p.getName()))
            team_joueur.remove(p.getName());
        String hostname = e.getHostname().toLowerCase();
        if (hostname.isEmpty())
            return;
        if (!hostname.contains(".minefight.fr"))
            return;
        String subdomain = hostname.split("\\.")[0];
        if (blacklist.contains(subdomain))
            return;
        team_joueur.put(p.getName(), subdomain);
    }

    @EventHandler
    public void onLogin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (!team_joueur.containsKey(p.getName())) return;
        String subdomain = team_joueur.get(p.getName());
        BarAPI.setMessage(p, "[" + ChatColor.GREEN + subdomain + ChatColor.RESET + "] Vous avez rejoint la team");
        Team t = SB.getTeam(subdomain);
        if (t == null) {
            t = SB.registerNewTeam(subdomain);
            t.setAllowFriendlyFire(false);
            t.setCanSeeFriendlyInvisibles(true);
        }
        broadcastTeamBarMessage(t, p.getDisplayName() + ChatColor.GOLD + " vous rejoint !");
        t.addPlayer(p);
        if (!scores.containsKey(t.getName()))
            scores.put(t.getName(), 0f);
        broadcastTeamBarHealth(t, scores.get(t.getName()));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        quit(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKick(PlayerKickEvent e) {
        quit(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();
        Player d = p.getKiller();
        if (d == null)
            return;
        Team tp = SB.getPlayerTeam(p);
        if (tp != null) {
            broadcastTeamBarMessage(tp, p.getDisplayName() + " a été tué par " + d.getDisplayName());
        }
        Team td = SB.getPlayerTeam(d);
        if (td != null) {
            broadcastTeamBarMessage(td, d.getDisplayName() + " a tué " + p.getDisplayName());
            float bonus = (float) (new Random().nextDouble() / 9);
            if (!scores.containsKey(td.getName()))
                scores.put(td.getName(), bonus);
            else
                scores.put(td.getName(), Math.max(1f, scores.get(td.getName()) + bonus));
            if (scores.get(td.getName()) == 1f) {
                int gain = Math.round((float) 2000 / td.getPlayers().size());
                shareMoney(td, gain);
                broadcastTeamMessage(td, Main.getTAG() + ChatColor.DARK_GREEN + "+2000ƒ" + ChatColor.RESET + " à partager avec ta team !");
                scores.put(td.getName(), 0f);
            }
            broadcastTeamBarHealth(td, scores.get(td.getName()));

        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        Team tp = SB.getPlayerTeam(p);
        if (tp != null) {
            broadcastTeamBarHealth(tp, scores.get(tp.getName()));
        }
    }
}
