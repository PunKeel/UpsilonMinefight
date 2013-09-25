package fr.PunKeel.Upsilon;

import de.kumpelblase2.remoteentities.CreateEntityContext;
import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * User: PunKeel
 * Date: 6/23/13
 * Time: 10:23 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class AntiCheat implements Listener {
    private Main main;
    private EntityManager manager;
    private List<UUID> honeypots = new ArrayList<>();
    private HashMap<String, Integer> spots = new HashMap<>();
    private HashMap<String, Integer> tests = new HashMap<>();

    public AntiCheat(final Main m) {
        main = m;
        manager = RemoteEntities.createManager(m);
    }

    public void testPlayer(final Player p) {
        CreateEntityContext context = manager.prepareEntity(RemoteEntityType.Human).asStationary(true).withName(new RandomString(16).nextString()).atLocation(Bukkit.getWorld(Main.WORLDNAME).getSpawnLocation());
        final RemoteEntity x = context.create();
        x.getBukkitEntity().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false), false);
        honeypots.add(x.getBukkitEntity().getUniqueId());
        x.getBukkitEntity().teleport(p.getLocation().add(0, 2.5, 1));
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
                    }
                }
            }
        }, 20);

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        for (Entity x : Bukkit.getWorld(Main.WORLDNAME).getEntities()) {
            if (honeypots.contains(x.getUniqueId()))
                if (x instanceof Player)
                    e.getPlayer().hidePlayer((Player) x);
        }
    }

    @EventHandler
    public void onInteract(EntityDamageByEntityEvent e) {
        if (honeypots.contains(e.getEntity().getUniqueId())) {
            LivingEntity d = null;
            if (e.getDamager() instanceof Player) {
                d = (Player) e.getDamager();
            }
            if (e.getDamager() instanceof Arrow) {
                d = ((Arrow) e.getDamager()).getShooter();
            }

            if (d != null && d instanceof Player) {
                String name = ((Player) d).getName();

                honeypots.remove(e.getEntity().getUniqueId());
                e.getEntity().remove();
                if (spots.containsKey(name)) {
                    spots.put(name, spots.get(name) + 1);
                } else {
                    spots.put(name, 1);
                }

                main.broadcastToAdmins(ChatColor.DARK_GRAY + "Suspect(" + spots.get(name) + "/" + tests.get(name) + ") : " + name);
            }
        }
    }
}

