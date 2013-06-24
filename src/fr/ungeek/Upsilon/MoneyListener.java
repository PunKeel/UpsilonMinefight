package fr.ungeek.Upsilon;

import com.github.games647.scoreboardstats.pvpstats.Database;
import com.github.games647.scoreboardstats.pvpstats.PlayerCache;
import com.google.common.base.Joiner;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;
import ru.tehkode.permissions.PermissionUser;

import java.util.*;


/**
 * User: PunKeel
 * Date: 5/9/13
 * Time: 6:43 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class MoneyListener implements Listener {
    public static final StateFlag FLAG_ARENE = new StateFlag("arene", false);
    Main main;
    ItemStack emerald;
    String[] morts;
    ProtectedRegion ameliorations;
    HashMap<String, Integer> kill_en_boucle = new HashMap<>();
    HashMap<String, String> victime_en_boucle = new HashMap<>();
    Set<String> invisi_players = new HashSet<>();

    public MoneyListener(Main m) {
        main = m;
        emerald = m.nameItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Menu principal", ChatColor.GRAY + "(Clic droit pour ouvrir)");
        morts = new String[]{"%s t'a tué", "Tu es mort de la main de %s", "%s est ton assassin", "%s est un meurtrier !", "%s a réussi à te tuer !", "Si tu veux te venger, c'est %s que tu dois tuer !"};

    }

    public void loadAmelioration() {
        ameliorations = main.RM.getRegion("amelioration");
        main.getWGCF().addCustomFlag(FLAG_ARENE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (p.getHealth() == 0) {
            Main.resetPlayer(p);
        }
        if (!p.getGameMode().equals(GameMode.ADVENTURE))
            if (!p.hasPermission("upsilon.admin"))
                p.setGameMode(GameMode.ADVENTURE);
        if (!p.hasPermission("upsilon.bypass_joinspawn")) {
            if (Main.getTimestamp() - main.ess.getUser(p).getLastLogout() > 10 || p.getHealth() != p.getMaxHealth())
                p.teleport(main.getWarp("sspawn"));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoinGain(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        String today = main.getDate();
        String yesterday = main.getDate(-1);
        final String message;
        if (p.hasMetadata("ups_lastjoin")) {
            List<MetadataValue> ups_lastjoin = p.getMetadata("ups_lastjoin");
            String LastJoin = "";
            if (ups_lastjoin.size() != 0) {
                LastJoin = p.getMetadata("ups_lastjoin").get(0).asString();
            }
            if (!LastJoin.equals(today)) {
                int jours = 1;
                p.setMetadata("ups_lastjoin", new FixedMetadataValue(main, today));
                int gain = 10;
                if (LastJoin.equals(yesterday)) {
                    // consecutif
                    jours = p.getMetadata("ups_follow").get(0).asInt() + 1;
                    gain = ((jours > 5) ? 50 : (jours * 10));
                    p.setMetadata("ups_follow", new FixedMetadataValue(main, jours));
                    message = (Main.getTAG() + "Tu as reçu " + gain + " ƒ pour tes " + jours + " jours de présence à la suite !");
                    main.econ.depositPlayer(p.getName(), gain);
                } else {
                    // pas consecutif
                    p.setMetadata("ups_follow", new FixedMetadataValue(main, jours));
                    message = Main.getTAG() + "Tu as reçu 10 ƒ pour ton premier jour de présence consécutif !";
                }
                if (!message.isEmpty()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(this.main, new Runnable() {
                        public void run() {
                            p.sendMessage(message);
                        }
                    }, 20);
                }
                main.econ.depositPlayer(p.getName(), gain);
            }

        } else {
            p.setMetadata("ups_lastjoin", new FixedMetadataValue(main, today));
            p.setMetadata("ups_follow", new FixedMetadataValue(main, 1));
        }

        if (!p.getInventory().containsAtLeast(emerald, 1)) {
            if (!p.getEnderChest().containsAtLeast(emerald, 1)) {
                if (p.getInventory().firstEmpty() != -1) {
                    p.getInventory().addItem(emerald);
                } else {
                    if (p.getEnderChest().firstEmpty() != -1) {
                        p.getInventory().addItem(emerald);
                    } else {
                        p.sendMessage(Main.getTAG() + "Ton inventaire est plein, vide le un peu et reconnecte toi pour obtenir une émeraude ! :)");
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        if (e.getItemDrop().getItemStack().isSimilar(emerald) && !main.isAdmin(p)) {
            e.setCancelled(true);
            return;
        }

        if ((e.getItemDrop().getItemStack().getType().equals(Material.FLINT) || e.getItemDrop().getItemStack().getType().equals(Material.GOLD_INGOT))) {
            main.econ.depositPlayer(p.getName(), e.getItemDrop().getItemStack().getAmount() * 5);
            p.sendMessage(Main.getTAG() + ChatColor.DARK_GREEN + "+" + ChatColor.RESET + e.getItemDrop().getItemStack().getAmount() * 5 + "ƒ pour la vente");
            e.getItemDrop().remove();
            return;
        }
        ApplicableRegionSet set = main.RM.getApplicableRegions(p.getLocation());
        if (set.allows(FLAG_ARENE)) {
            e.setCancelled(true);
            p.sendMessage(Main.getTAG() + "Pour ta sécurité, le drop d'item est interdit en arêne");
            p.sendMessage(Main.getTAG() + "Pour envoyer un objet, tu peux faire " + ChatColor.ITALIC + "/sendto <pseudo>" + ChatColor.RESET + " ou sortir de l'arêne");
            return;
        }
    }

    /*@EventHandler(ignoreCancelled = true)
    public void flintListener(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (e.getItem() != null) {
                if (e.getItem().getType().equals(Material.FLINT)) {
                    TagAPI.refreshPlayer(p);
                    selling.put(p.getName(), Main.getTimestamp());
                    p.sendMessage(Main.getTAG() + "Jette tes silex " + ChatColor.GRAY + "(Flint)" + ChatColor.RESET + " pour les vendre ! (Tu as 10 secondes)");
                    e.setCancelled(true);
                }
            }
        }
    }*/

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        Location loc = p.getLocation();
        ApplicableRegionSet set = main.RM.getApplicableRegions(loc);
        for (ProtectedRegion PR : set) {
            String name = PR.getId();
            if (main.SM.exists(name)) {
                if (set.allows(FLAG_ARENE)) {
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
                                }
                                c.showPlayer(p);
                            }
                        }
                    }, 20 * 5);
                }
                e.setRespawnLocation(main.SM.getRandom(name).toLocation());
                return;
            }
        }

        e.setRespawnLocation(main.getWarp("sspawn"));
    }

    public String deathMessage(Player p) {
        if (p.getLastDamageCause() == null) {
            return "";
        }
        EntityDamageEvent damageEvent = p.getLastDamageCause();
        EntityDamageEvent.DamageCause damageCause = damageEvent.getCause();
        if (damageEvent instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent kie = (EntityDamageByEntityEvent) damageEvent;
            Entity damager = kie.getDamager();
            if (damageCause == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                if (damager instanceof Player) {
                    Player attackp = (Player) damager;
                    return (p.getDisplayName() + " a été tué par " + attackp.getDisplayName() + " aidé d" + (attackp.getItemInHand().getType() == Material.AIR ? "e ses poings" : attackp.getItemInHand().getType() == Material.WOOD_SWORD ? "'une épée en bois" : attackp.getItemInHand().getType() == Material.STONE_SWORD ? "'une épée en pierre" : attackp.getItemInHand().getType() == Material.IRON_SWORD ? "'une épée en fer" : attackp.getItemInHand().getType() == Material.GOLD_SWORD ? "'une épée en or" : attackp.getItemInHand().getType() == Material.DIAMOND_SWORD ? "'une épée en diamant" : attackp.getItemInHand().getTypeId() < 256 ? "'un bloc" : attackp.getItemInHand().getTypeId() >= 256 ? "'un item" : "'un item"));
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
                return (p.getDisplayName() + " est tombééééééééééééé... dans le vide");
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

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent e) {
        Player v = e.getEntity();
        if (v.hasMetadata("NPC")) return;
        if (v == null) return;
        Location loc = v.getLocation();
        ApplicableRegionSet set = main.RM.getApplicableRegions(loc);
        if (!set.allows(FLAG_ARENE)) {
            e.setDeathMessage(deathMessage(v));
            return;
        }
        e.setDeathMessage("");
        Player d = v.getKiller();
        if (d == null) return;
        if (!d.isOnline()) return;
        if (d.getName().equals(v.getName())) return;
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
            PlayerCache stats = Database.getCache(v.getName());
            int gain = (int) (Math.sqrt(stats.getKills() ^ 2 / (stats.getDeaths() + 1)) / 2) + 1;
            if (gain > 25) gain = 25;
            gain += stats.getLastStreak();
            d.sendMessage(Main.getTAG() + ChatColor.DARK_GREEN + "+ " + ChatColor.GOLD + gain + ChatColor.RESET + "ƒ pour le kill de " + v.getDisplayName());
            main.econ.depositPlayer(d.getName(), gain);
        }
        v.sendMessage(Main.getTAG() + Main.getRandom(morts).replaceAll("%s", d.getDisplayName()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        for (Player x : Bukkit.getOnlinePlayers()) {
            x.showPlayer(p);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAmelioration(BlockPlaceEvent e) {
        if (e.isCancelled()) return;
        Block b = e.getBlock();
        if (b.getType().equals(Material.WALL_SIGN)) {
            Location loc = b.getLocation();
            if (ameliorations.contains(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ())) {
                main.broadcastToAdmins(Main.getTAG() + ChatColor.DARK_GRAY + e.getPlayer().getDisplayName() + " a proposé une amélioration", true);
            }
        }
    }

    @EventHandler
    public void onGMChange(PlayerGameModeChangeEvent e) {
        if (Thread.currentThread().getId() != main.getMainThreadName()) return;
        if (e.getPlayer().hasMetadata("NPC")) return;
        TagAPI.refreshPlayer(e.getPlayer());
    }

    @EventHandler
    public void onFlyChange(PlayerToggleFlightEvent e) {
        if (Thread.currentThread().getId() != main.getMainThreadName()) return;
        if (e.getPlayer().hasMetadata("NPC")) return;
        TagAPI.refreshPlayer(e.getPlayer());
    }

    @EventHandler
    public void onTeleportPlayer(PlayerTeleportEvent e) {
        if (Thread.currentThread().getId() != main.getMainThreadName()) return;
        if (e.getPlayer().hasMetadata("NPC")) return;
        if (new Random().nextBoolean())
            if (e.getPlayer().isOnline())
                TagAPI.refreshPlayer(e.getPlayer());
    }

    @EventHandler
    public void onNameTag(PlayerReceiveNameTagEvent e) {
        PermissionUser p = main.PEX.getUser(e.getNamedPlayer());
        String trash = ChatColor.RESET.toString();
        int junk = (16 - e.getTag().length()) / 2;
        if (junk == 0) return;
        Random rand = new Random();
        List<String> pseudo = new ArrayList<>();
        for (char i : e.getTag().toCharArray()) {
            pseudo.add(String.valueOf(i));
        }
        if (GameMode.CREATIVE.equals(e.getNamedPlayer().getGameMode()) || e.getNamedPlayer().isFlying()) {
            if (p.inGroup("admin")) {
                pseudo.add(0, ChatColor.RED.toString());
                junk--;
                trash = ChatColor.RED.toString();
            } else if (p.inGroup("modo") || p.inGroup("co-admin")) {
                junk--;
                pseudo.add(0, ChatColor.BLUE.toString());
                trash = ChatColor.BLUE.toString();
            }
        }
        for (int i = 0; i < junk; i++) {
            pseudo.add(rand.nextInt(pseudo.size()), trash);
        }
        e.setTag(Joiner.on("").skipNulls().join(pseudo));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        List<String> strings = Arrays.asList(ChatColor.stripColor(e.getMessage().toLowerCase()).split("[^\\w]+"));
        for (OfflinePlayer p : Bukkit.getOperators()) {
            if (!p.isOnline()) continue;
            if (p.getName().equals(e.getPlayer().getName())) continue;
            if (strings.contains(p.getName().toLowerCase()) || strings.contains(ChatColor.stripColor(((Player) p).getDisplayName().toLowerCase()))) {
                Main.alert((Player) p, false);
            }
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(PlayerChatEvent e) {

        String[] mots = e.getMessage().split(" ");
        Player i = e.getPlayer();
        switch (mots[0].toLowerCase()) {
            case "!mumble":
                i.sendMessage(Main.getTAG() + "Mumble : " + ChatColor.DARK_GREEN + "mumble.minefight.fr" + ChatColor.RESET + " Port : " + ChatColor.DARK_GREEN + "50470");
                e.setCancelled(true);
                return;
            case "!vote":
                i.sendMessage(Main.getTAG() + "Lien de vote : " + ChatColor.DARK_GREEN + "http://para.ms/vote-MF/" + e.getPlayer().getName());
                e.setCancelled(true);
                return;
            case "!vip":
                i.sendMessage(Main.getTAG() + "Pour devenir " + ChatColor.GOLD + "VIP" + ChatColor.RESET + ", il suffit de te rendre sur :" + ChatColor.DARK_GREEN + " http://minefight.fr/monnaie-virtuelle/");
                e.setCancelled(true);
                return;
            case "!youtube":
            case "!yt":
                i.sendMessage(Main.getTAG() + "Rejoins la chaine Youtube officielle Minefight : " + ChatColor.DARK_GREEN + " http://bit.ly/YT-MF");
                e.setCancelled(true);
                return;
            case "!votekick":
                if (i.hasPermission("upsilon.votekick")) {
                    if (mots.length == 2) {
                        Player c = Bukkit.getPlayerExact(mots[1]);
                        if (c == null) {
                            i.sendMessage(Main.getTAG() + "Ce joueur n'est pas en ligne");
                            e.setCancelled(true);
                            return;
                        }
                        if (c.hasPermission("upsilon.antivotekick")) {
                            i.sendMessage(Main.getTAG() + "Ce joueur est protégé contre le votekick !");
                            e.setCancelled(true);
                            return;
                        }
                        if (!main.votekick.containsKey(mots[1])) {
                            main.votekick.put(mots[1], new VoteKickHolder());
                        }
                        if (main.votekick.get(mots[1]).add(i.getName())) {
                            if (main.votekick.get(mots[1]).shouldKick()) {
                                c.kickPlayer(Main.getTAG() + ChatColor.RED + "Kick par la communauté Minefight");
                                return;
                            } else {
                                i.sendMessage(Main.getTAG() + "VoteKick " + mots[1] + " | Y:" + main.votekick.get(mots[1]).votes);
                            }
                        } else {
                            e.setCancelled(true);
                            return;
                        }
                        break;
                    } else {
                        i.sendMessage(Main.getTAG() + "Usage : !votekick <pseudo>");
                    }
                }
                break;
            case "!classement":
            case "!rank":
                switch (Database.getCache(i.getName()).getRank()) {
                    case 1:
                        i.sendMessage(Main.getTAG() + ChatColor.GOLD + "Tu es premier");
                        break;
                    case 2:
                        i.sendMessage(Main.getTAG() + ChatColor.GREEN + "Tu es deuxième, un petit effort pour passer premier !");
                        break;
                    case 3:
                        i.sendMessage(Main.getTAG() + ChatColor.GREEN + "Tu es troisième, un petit effort pour passer deuxième !");
                        break;
                    default:
                        i.sendMessage(Main.getTAG() + ChatColor.DARK_GREEN + "Tu es classé #" + ChatColor.DARK_RED + Database.getCache(i.getName()).getRank());
                        break;
                }
                e.setCancelled(true);
                return;
            /*case "!add":
                if (e.getPlayer().getName().equalsIgnoreCase(mots[1]))
                    e.getPlayer().sendMessage(Main.getTAG() + "Tu es déjà ton propre ami !");
                else
                    main.FM.addFriend(e.getPlayer().getName(), mots[1]);
            case "!status":
                if (!e.getPlayer().getName().equalsIgnoreCase(mots[1])) {
                    FriendManager.STATUS S = main.FM.getStatus(e.getPlayer().getName(), mots[1]);
                    if (S.equals(FriendManager.STATUS.FRIENDS))
                        e.getPlayer().sendMessage(Main.getTAG() + "Tu es ami avec " + mots[1]);
                    else if (S.equals(FriendManager.STATUS.NULL))
                        e.getPlayer().sendMessage(Main.getTAG() + "Tu n'as aucun lien avec " + mots[1]);
                    else if (S.equals(FriendManager.STATUS.INVITED))
                        e.getPlayer().sendMessage(Main.getTAG() + mots[1] + " aimerait bien être ami avec toi");
                    else if (S.equals(FriendManager.STATUS.WAITING_REPLY))
                        e.getPlayer().sendMessage(Main.getTAG() + mots[1] + " n'a pas répondu à ton invitation");
                }
                e.setCancelled(true);
                return;  */
            case "!help":
                i.sendMessage(Main.getTAG() + "Commandes disponibles :");
                i.sendMessage("!classement");
                i.sendMessage("!votekick <pseudo>");
                i.sendMessage("!vip");
                i.sendMessage("!youtube");
                i.sendMessage("!mumble");
                i.sendMessage("!vote");
                e.setCancelled(true);
                return;
            default:
                break;
        }
    }
}

class VoteKickHolder {
    int votes, last_vote;
    ArrayList<String> voters = new ArrayList<>();

    VoteKickHolder() {
        this.votes = 0;
        this.last_vote = 0;
    }

    public void reset() {
        this.votes = 0;
        this.last_vote = 0;
        this.voters.clear();
    }

    public boolean add(String p) {
        if (last_vote > Main.getTimestamp() + 70) reset();
        if (voters.contains(p)) return false;
        voters.add(p);
        votes++;
        last_vote = Main.getTimestamp();
        return true;
    }

    public boolean shouldKick() {
        int online = Bukkit.getOnlinePlayers().length;
        if (online < 10) {
            return votes > online / 2;
        } else {
            return votes >= online / 3;
        }
    }
}