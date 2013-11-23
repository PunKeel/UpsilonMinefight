package fr.PunKeel.Upsilon;


import de.kumpelblase2.remoteentities.CreateEntityContext;
import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: PunKeel
 * Date: 6/23/13
 * Time: 10:23 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */


/**
 * User: PunKeel
 * Date: 6/23/13
 * Time: 10:23 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class AntiCheat implements Listener {
    Main main;
    private List<UUID> honeypots = new ArrayList<>();
    private HashMap<String, Integer> spots = new HashMap<>();
    private HashMap<String, Integer> tests = new HashMap<>();
    private HashMap<String, Integer> successifs = new HashMap<>();
    CreateEntityContext context;
    int SCORE_HIT = 3, SCORE_PROJECTILE = 1;

    public AntiCheat(final Main m) {
        main = m;
        EntityManager manager = RemoteEntities.createManager(m);
        context = manager.prepareEntity(RemoteEntityType.Human);
        context.asStationary(true);
    }

    public void saveLog() {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(main.getDataFolder() + "/cheat.log", true)));
            out.println("### " + new SimpleDateFormat("dd.MM.YYYY").format(new Date()) + " ###");
            for (String p : tests.keySet()) {
                if (getScore(p).startsWith("[0"))
                    continue;
                out.println(p + ": " + getScore(p));
            }

            out.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            main.getLogger().severe("Could not save anti cheat file :(");
        }

    }


    public void testPlayer(final Player p) {
        if (honeypots.contains(p.getUniqueId()))
            return;
        if (honeypots.size() >= 15)
            return;
        final RemoteEntity x = context.atLocation(p.getEyeLocation().add(0, 2, 0)).withName(new RandomString(16).nextString()).create();
        x.getBukkitEntity().setMetadata("NPC", new FixedMetadataValue(main, true));
        x.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false), false);
        honeypots.add(x.getBukkitEntity().getUniqueId());
        x.getBukkitEntity().teleport(p.getEyeLocation().add(0, 2, 0));
        for (Player k : Bukkit.getOnlinePlayers()) {
            if (k.getName().equals(p.getName())) continue;
            k.hidePlayer((Player) x.getBukkitEntity());
        }

        if (tests.containsKey(p.getName())) {
            tests.put(p.getName(), tests.get(p.getName()) + 1);
        } else {

            tests.put(p.getName(), 1);
        }

        Bukkit.getScheduler().runTaskLater(main, new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;
                if (x.getBukkitEntity() != null && i <= 6 && p.isOnline()) {
                    x.getBukkitEntity().teleport(p.getEyeLocation().add(0, 2, 0));
                    Bukkit.getScheduler().runTaskLater(main, this, 20);
                } else {
                    if (x.getBukkitEntity() != null) {
                        x.getBukkitEntity().remove();
                        honeypots.remove(x.getBukkitEntity().getUniqueId());
                        successifs.remove(p.getName());
                    }
                }
            }
        }, 20);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Entity x : e.getPlayer().getWorld().getEntities()) {
            if (honeypots.contains(x.getUniqueId()))
                if (x instanceof Player)
                    e.getPlayer().hidePlayer((Player) x);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (honeypots.contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent e) {
        if (honeypots.contains(e.getEntity().getUniqueId())) {
            e.setCancelled(true);
            Player d = null;
            int score = SCORE_HIT;
            if (e.getDamager() instanceof Player) {
                d = (Player) e.getDamager();
            }
            if (e.getDamager() instanceof Projectile) {
                if (((Projectile) e.getDamager()).getShooter() instanceof Player) {
                    d = (Player) ((Projectile) e.getDamager()).getShooter();
                    score = SCORE_PROJECTILE;
                }
            }


            if (d != null && d instanceof Player) {
                String name = d.getName();

                honeypots.remove(e.getEntity().getUniqueId());
                e.getEntity().remove();
                if (spots.containsKey(name)) {
                    spots.put(name, spots.get(name) + score);
                } else {
                    spots.put(name, score);
                }
                if (successifs.containsKey(name)) {
                    successifs.put(name, successifs.get(name) + score);
                } else {
                    successifs.put(name, score);
                }
                if (successifs.get(name) >= 3 * SCORE_HIT)
                    kick(d);
                for (Player p : Bukkit.getOnlinePlayers())
                    if (p.isOp())
                        p.sendMessage(ChatColor.DARK_GRAY + "Suspect: " + name + " " + getScore(name));
            }
        }
    }

    public void kick(Player p) {
        if (p.isOp())
            p.sendMessage(ChatColor.DARK_RED + "Suspecté de triche. " + getScore(p));
        else {
            p.kickPlayer("Suspecté de triche " + getScore(p));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format("tempban %s 3600", p.getName()));
            if (p.isOnline())
                p.kickPlayer(ChatColor.DARK_RED + "Suspecté de triche " + getScore(p));
        }
    }

    public String getScore(Player p) {
        return getScore(p.getName());
    }

    public String getScore(String name) {
        if (tests.containsKey(name))
            if (spots.containsKey(name))
                return "[" + (spots.get(name) / SCORE_HIT) + "/" + tests.get(name) + "]";
            else
                return "[0/" + tests.get(name) + "]";
        else
            return "[-]";
    }
}