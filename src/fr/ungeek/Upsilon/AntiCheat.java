package fr.ungeek.Upsilon;

import de.kumpelblase2.remoteentities.CreateEntityContext;
import de.kumpelblase2.remoteentities.EntityManager;
import de.kumpelblase2.remoteentities.RemoteEntities;
import de.kumpelblase2.remoteentities.api.RemoteEntity;
import de.kumpelblase2.remoteentities.api.RemoteEntityType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * User: PunKeel
 * Date: 6/23/13
 * Time: 10:23 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class AntiCheat implements Listener {
    Main main;
    EntityManager manager;
    List<UUID> honeypots = new ArrayList<>();
    HashMap<String, Integer> spots = new HashMap<>();

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
        Bukkit.getScheduler().runTaskLater(main, new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;
                if (x.getBukkitEntity() != null && i <= 6) {
                    x.getBukkitEntity().teleport(p.getEyeLocation().add(0, 2, 0));
                    Bukkit.getScheduler().runTaskLater(main, this, 20);
                    main.getLogger().info(i + "");
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

                main.broadcastToAdmins(ChatColor.DARK_GRAY + "Suspect(" + spots.get(name) + ") : " + name);
            }
        }
    }
}

class RandomString {

    private static final char[] symbols = new char[36];

    static {
        for (int idx = 0; idx < 10; ++idx)
            symbols[idx] = (char) ('0' + idx);
        for (int idx = 10; idx < 36; ++idx)
            symbols[idx] = (char) ('a' + idx - 10);
    }

    private final Random random = new Random();
    private final char[] buf;

    public RandomString(int length) {
        if (length < 1)
            throw new IllegalArgumentException("length < 1: " + length);
        buf = new char[length];
    }

    public String nextString() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

}