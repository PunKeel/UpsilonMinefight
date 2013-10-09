package fr.PunKeel.Upsilon;

import com.github.games647.scoreboardstats.pvpstats.Database;
import com.github.games647.scoreboardstats.pvpstats.PlayerCache;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.PunKeel.Upsilon.BarAPI.FakeDragon;
import fr.PunKeel.Upsilon.BarAPI.General;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * User: PunKeel
 * Date: 10/9/13
 * Time: 8:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class DeathManager implements Listener {
    Main main;
    String[] morts = new String[]{"%s t'a tué", "Tu es mort de la main de %s", "%s est ton assassin", "%s est un meurtrier !", "%s a réussi à te tuer !", "Si tu veux te venger, c'est %s que tu dois tuer !"};
    private HashMap<Integer, String> congrats = new HashMap<>();
    private HashMap<String, Integer> kill_en_boucle = new HashMap<>();
    private HashMap<String, String> victime_en_boucle = new HashMap<>();
    private Set<String> invisi_players = new HashSet<>();
    private HashMap<String, Integer> killstreaks = new HashMap<>();

    public DeathManager(Main m) {
        main = m;
        congrats.put(2, "Double kill");
        congrats.put(3, "Triple kill");
        congrats.put(4, "Quadra kill");
        congrats.put(5, "Penta kill");
        congrats.put(6, "Legendary kill");
        congrats.put(10, "Killing spree");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        FakeDragon.setStatus(p, null, -1);
        Location loc = p.getLocation();
        PlayerCache statsv = Database.getCache(p.getName());
        long ratio = (long) (1 + statsv.getKills()) / (long) (1 + statsv.getDeaths());
        if (ratio <= 0.4) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.getById(22), 20 * 30, 1));
        }
        ApplicableRegionSet set = WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(loc);
        for (ProtectedRegion PR : set) {
            String name = PR.getId();
            if (main.SM.exists(name)) {
                if (set.allows(Main.FLAG_ARENE)) {
                    for (Player c : Bukkit.getOnlinePlayers()) {
                        c.hidePlayer(p);
                        p.hidePlayer(c);
                    }
                    p.sendMessage(Main.getTAG() + "Protection de 5 secondes :" + ChatColor.DARK_GREEN + " ON");
                    invisi_players.add(p.getName());
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                        @Override
                        public void run() {
                            if (!p.isOnline()) return;
                            p.sendMessage(Main.getTAG() + "Protection de 5 secondes :" + ChatColor.DARK_RED + " OFF");
                            invisi_players.remove(p.getName());
                            for (Player c : Bukkit.getOnlinePlayers()) {
                                if (p.getName().equals(c.getName())) continue;
                                if (invisi_players.contains(c.getName())) continue;
                                if (!main.ess.getUser(c).isVanished()) {
                                    p.showPlayer(c);
                                    c.showPlayer(p);
                                }
                            }
                        }
                    }, 20 * 5);
                }
                e.setRespawnLocation(main.SM.getRandom(name, loc).toLocation());
                return;
            }
        }

        e.setRespawnLocation(main.getWarp("spawn"));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent e) {
        final Player v = e.getEntity();
        if (v.hasPermission("upsilon.bypass_death_screen")) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                @Override
                public void run() {
                    Class<?> PacketClientCommand = General.getCraftClass("Packet205ClientCommand");
                    Object packet;
                    try {

                        packet = PacketClientCommand.newInstance();

                        Field a = General.getField(PacketClientCommand, "a");
                        a.setAccessible(true);
                        a.set(packet, 1);
                        General.receivePacket(v, packet);
                    } catch (IllegalAccessException | InstantiationException ignored) {

                    }
                }
            }, 0L);
        }

        if (killstreaks.containsKey(v.getName())) killstreaks.remove(v.getName());

        if (v.hasMetadata("NPC")) return;
        Location loc = v.getLocation();
        ApplicableRegionSet set = WGBukkit.getRegionManager(v.getWorld()).getApplicableRegions(loc);
        e.setDeathMessage("");
        if (Bukkit.getOnlinePlayers().length <= 25)
            Bukkit.broadcastMessage(getDeathMessage(v));
        if (!set.allows(Main.FLAG_ARENE)) {
            return;
        }
        e.setDroppedExp(v.getTotalExperience() % 300);
        Player d = v.getKiller();
        if (d == null) return;
        if (!d.isOnline()) return;
        if (d.getName().equals(v.getName())) return;
        if (killstreaks.containsKey(d.getName()))
            killstreaks.put(d.getName(), killstreaks.get(d.getName()) + 1);
        else
            killstreaks.put(d.getName(), 1);
        int success;
        if (kill_en_boucle.containsKey(d.getName())) {
            success = kill_en_boucle.get(d.getName());
            if (victime_en_boucle.get(d.getName()).equals(v.getName())) {
                success++;
            } else {
                success = 1;
            }
        } else {
            success = 1;
        }
        kill_en_boucle.put(d.getName(), success);
        victime_en_boucle.put(d.getName(), v.getName());
        if (success > 3) {
            d.sendMessage(Main.getTAG() + ChatColor.DARK_RED + "Pas de gain car kill répété sur " + ChatColor.RESET + v.getDisplayName());
        } else {
            Calendar c = Calendar.getInstance();
            int gain = (c.getActualMaximum(Calendar.DAY_OF_MONTH) - c.get(Calendar.DAY_OF_MONTH)) * 4 / 5; // Permet de gagner plus au début du mois (=> reset auto)
            PlayerCache statsv = Database.getCache(v.getName());
            PlayerCache statsa = Database.getCache(d.getName());
            if (statsa.getKills() > statsv.getKills() && statsa.getDeaths() > statsv.getDeaths()) {
                // Victime moins forte que attaquant
                gain += (statsa.getKills() - statsv.getKills()) / (statsa.getDeaths() + statsv.getDeaths()) * 25;
            } else {
                // Victime plus forte qu'attaquant
                gain += (int) (Math.sqrt(statsv.getKills() ^ 2 / (statsv.getDeaths() + 1)) / 2) + 1;
                if (killstreaks.containsKey(v.getName()))
                    gain += killstreaks.get(v.getName());
            }
            gain = Math.max(1, Math.min(gain, 50)); // gain entre 1 et 50 :D
            if (main.isVIP(d))
                gain = (int) (gain * 1.25);
            d.sendMessage(Main.getTAG() + ChatColor.DARK_GREEN + "+ " + ChatColor.GOLD + gain + ChatColor.RESET + "ƒ pour le kill de " + v.getDisplayName());

            if (congrats.containsKey(killstreaks.get(d.getName())))
                d.sendMessage("" + ChatColor.DARK_GREEN + ChatColor.BOLD + congrats.get(statsa.getStreak()));
            main.addToBalance(d.getName(), gain);

        }
        v.sendMessage(Main.getTAG() + Main.getRandom(morts).replaceAll("%s", d.getDisplayName()));
    }

    String getDeathMessage(Player p) {
        if (p.getLastDamageCause() == null) {
            return null;
        }
        EntityDamageEvent damageEvent = p.getLastDamageCause();
        EntityDamageEvent.DamageCause damageCause = damageEvent.getCause();
        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent kie = (EntityDamageByEntityEvent) damageEvent;
            Entity damager = kie.getDamager();
            if (damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if (damager instanceof Player) {
                    Player attackp = (Player) damager;
                    return (p.getDisplayName() + " a été tué par " + attackp.getDisplayName());
                } else if (damager instanceof PigZombie) {
                    return (p.getDisplayName() + " a été tué par un Pigman");
                } else if (damager instanceof Zombie) {
                    return (p.getDisplayName() + " a été tué par un zombie");
                } else if (damager instanceof CaveSpider) {
                    return (p.getDisplayName() + " a été tué par une araignée des caves");
                } else if (damager instanceof Spider) {
                    return (p.getDisplayName() + " a été tué par une araignée");
                } else if (damager instanceof Enderman) {
                    return (p.getDisplayName() + " a perdu contre un Enderman");
                } else if (damager instanceof Silverfish) {
                    return (p.getDisplayName() + " a croisé un Silverfish");
                } else if (damager instanceof MagmaCube) {
                    return (p.getDisplayName() + " a été tué par un Magma Slime");
                } else if (damager instanceof Slime) {
                    return (p.getDisplayName() + " a été tué par un Slime");
                } else if (damager instanceof Wolf) {
                    return (p.getDisplayName() + " a été dévoré par un loup");
                } else if (damager instanceof IronGolem) {
                    return (p.getDisplayName() + " a voulu se battre contre un golem de fer");
                } else if (damager instanceof Giant) {
                    return (p.getDisplayName() + " a été tué par un Géant");
                }
            } else if (damageCause == EntityDamageEvent.DamageCause.PROJECTILE) {
                Projectile pro = (Projectile) damager;
                if (pro.getShooter() instanceof Player) {
                    Player attackp = (Player) pro.getShooter();
                    if (pro instanceof Arrow) {
                        return (p.getDisplayName() + " a été tué par la flêche de " + attackp.getDisplayName());
                    } else if (pro instanceof Snowball) {
                        return (p.getDisplayName() + " est mort de la boule de neige de " + attackp.getDisplayName());
                    } else if (pro instanceof Egg) {
                        return (p.getDisplayName() + " est mort en recevant un oeuf de " + attackp.getDisplayName());
                    } else {
                        return (p.getDisplayName() + " est mort du projectile de " + attackp.getDisplayName());
                    }
                }
                if (pro instanceof Arrow) {
                    if ((pro.getShooter() instanceof Skeleton)) {
                        return ("Un squelette a osé tuer " + p.getDisplayName() + ChatColor.GRAY + " (bien fait!)");
                    } else {
                        return (p.getDisplayName() + " est mort à cause d'une fèche");
                    }
                } else if (pro instanceof Snowball) {
                    return (p.getDisplayName() + " a été tué par un bonhomme de neige");
                } else if (pro instanceof Fireball) {
                    if (pro.getShooter() instanceof Ghast) {
                        return (p.getDisplayName() + " a été tué par une boule de feu");
                    } else if ((pro.getShooter() instanceof Blaze)) {
                        return (p.getDisplayName() + " a été tué par un blaze");
                    } else if ((pro.getShooter() instanceof Wither)) {
                        return (p.getDisplayName() + " a été tué par le Wither");
                    } else {
                        return (p.getDisplayName() + " a été tué par une boule de feu");
                    }
                }
            } else if (damageCause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                if (damager instanceof Creeper) {
                    return (p.getDisplayName() + " s'est fait exploser par un creeper");
                } else if (damager instanceof TNTPrimed) {
                    return (p.getDisplayName() + " a trop joué avec la TNT");
                }
            }
        } else {
            if (damageCause == EntityDamageEvent.DamageCause.DROWNING) {
                return (p.getDisplayName() + " s'est noyé");
            } else if (damageCause == EntityDamageEvent.DamageCause.STARVATION) {
                return (p.getDisplayName() + " est mort de faim");
            } else if (damageCause == EntityDamageEvent.DamageCause.CONTACT) {
                return (p.getDisplayName() + " s'est frotté à un cactus");
            } else if (damageCause == EntityDamageEvent.DamageCause.CUSTOM) {
                return (p.getDisplayName() + " est mort.");
            } else if (damageCause == EntityDamageEvent.DamageCause.FIRE) {
                return (p.getDisplayName() + " a pris feu");
            } else if (damageCause == EntityDamageEvent.DamageCause.FIRE_TICK) {
                return (p.getDisplayName() + " a pris feu");
            } else if (damageCause == EntityDamageEvent.DamageCause.LAVA) {
                return (p.getDisplayName() + " a nagé dans la lave. Sans succès.");
            } else if (damageCause == EntityDamageEvent.DamageCause.LIGHTNING) {
                return (p.getDisplayName() + " a été foudroyé");
            } else if (damageCause == EntityDamageEvent.DamageCause.POISON) {
                return ("On a empoisonné " + p.getDisplayName());
            } else if (damageCause == EntityDamageEvent.DamageCause.SUFFOCATION) {
                return (p.getDisplayName() + " a suffoqué");
            } else if (damageCause == EntityDamageEvent.DamageCause.VOID) {
                return (p.getDisplayName() + " est tombé ... dans le vide");
            } else if (damageCause == EntityDamageEvent.DamageCause.FALL) {
                return (p.getDisplayName() + " est tombé");
            } else if (damageCause == EntityDamageEvent.DamageCause.SUICIDE) {
                return (p.getDisplayName() + " s'est ... suicidé");
            } else if (damageCause == EntityDamageEvent.DamageCause.MAGIC) {
                return (p.getDisplayName() + " a été tué par la magie");
            } else if (damageCause == EntityDamageEvent.DamageCause.WITHER) {
                return p.getDisplayName() + " a été tué par le Wither";
            }
        }
        return (p.getDisplayName() + " est mort.");
    }

}
