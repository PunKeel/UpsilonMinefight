package fr.PunKeel.Upsilon;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.github.games647.scoreboardstats.pvpstats.Database;
import com.github.games647.scoreboardstats.pvpstats.PlayerCache;
import com.google.common.base.Joiner;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.PunKeel.Upsilon.BarAPI.FakeDragon;
import fr.PunKeel.Upsilon.BarAPI.General;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.kitteh.tag.PlayerReceiveNameTagEvent;
import org.kitteh.tag.TagAPI;
import ru.tehkode.permissions.PermissionUser;

import java.lang.reflect.Field;
import java.util.*;


/**
 * User: PunKeel
 * Date: 5/9/13
 * Time: 6:43 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class MoneyListener implements Listener {
    private static final StateFlag FLAG_ARENE = new StateFlag("arene", false);
    private static final StateFlag FLAG_DIE_ON_LEAVE = new StateFlag("die_on_leave", false);
    Set<String> already = new HashSet<>();
    private Main main;
    private ItemStack emerald;
    private String[] morts;
    private HashMap<String, Integer> kill_en_boucle = new HashMap<>();
    private HashMap<String, String> victime_en_boucle = new HashMap<>();
    private Set<String> invisi_players = new HashSet<>();
    private HashMap<Integer, String> congrats = new HashMap<>();
    private HashMap<String, Integer> killstreaks = new HashMap<>();

    public MoneyListener(Main m) {
        main = m;
        emerald = m.nameItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Menu principal", ChatColor.GRAY + "(Clic droit pour ouvrir)");
        morts = new String[]{"%s t'a tué", "Tu es mort de la main de %s", "%s est ton assassin", "%s est un meurtrier !", "%s a réussi à te tuer !", "Si tu veux te venger, c'est %s que tu dois tuer !"};
        congrats.put(2, "Double kill");
        congrats.put(3, "Triple kill");
        congrats.put(4, "Quadra kill");
        congrats.put(5, "Penta kill");
        congrats.put(6, "Legendary kill");
        congrats.put(10, "Killing spree");

    }

    void removeFor(Player p) {
        if (!already.contains(p.getName())) {
            if (p.getInventory().contains(Material.SKULL_ITEM) || p.getEnderChest().contains(Material.SKULL_ITEM)) {
                p.getInventory().remove(Material.SKULL_ITEM);
                p.getEnderChest().remove(Material.SKULL_ITEM);
                p.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 2));
                p.updateInventory();
                p.sendMessage(Main.getTAG() + ChatColor.BLUE + "Suite au passage en 1.6, l'économie change et nous devons confisquer tes têtes. En échange, tu as eu du fer :)");
            }
            already.add(p.getName());
        }
    }

    @EventHandler
    public void onAnvilChange(EntityChangeBlockEvent e) {
        if (!e.getBlock().getType().equals(Material.ANVIL)) return;
        if (!e.getTo().equals(Material.AIR)) return;
        e.setCancelled(true);
    }

    public void loadAmelioration() {
        main.getWGCF().addCustomFlag(FLAG_ARENE);
        main.getWGCF().addCustomFlag(FLAG_DIE_ON_LEAVE);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        removeFor(p);
        if (p.getHealth() == 0) {
            Main.resetPlayer(p);
        }
        if (!p.getGameMode().equals(GameMode.ADVENTURE) && !p.hasPermission("upsilon.admin"))
            p.setGameMode(GameMode.ADVENTURE);

    }

    @EventHandler(ignoreCancelled = true)
    public void onJoinGain(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (p.getName().equals("DleoT"))
            if (!p.hasPermission("upsilon.bypass_joinspawn"))
                if ((1000 * Main.getTimestamp() - main.ess.getUser(p).getLastLogout()) >= 10)
                    Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                        @Override
                        public void run() {
                            main.teleportToWarp("spawn", p);
                        }
                    });
        if (!p.getInventory().containsAtLeast(emerald, 1)) {
            if (!p.getEnderChest().containsAtLeast(emerald, 1)) {
                if (p.getInventory().firstEmpty() != -1) {
                    p.getInventory().addItem(emerald);
                    p.updateInventory();
                } else {
                    if (p.getEnderChest().firstEmpty() != -1) {
                        p.getEnderChest().addItem(emerald);
                    } else {
                        p.sendMessage(Main.getTAG() + "Ton inventaire est plein, vide le un peu et reconnecte toi pour obtenir une émeraude ! :)");
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(CraftItemEvent e) {
        HashSet<Material> interdits = new HashSet<>();
        interdits.add(Material.GOLD_AXE);
        interdits.add(Material.GOLD_BOOTS);
        interdits.add(Material.GOLD_CHESTPLATE);
        interdits.add(Material.GOLD_HELMET);
        interdits.add(Material.GOLD_HOE);
        interdits.add(Material.GOLD_LEGGINGS);
        interdits.add(Material.GOLD_PICKAXE);
        interdits.add(Material.GOLDEN_CARROT);
        interdits.add(Material.GOLDEN_APPLE);
        interdits.add(Material.GOLD_SPADE);
        interdits.add(Material.GOLD_SWORD);

        interdits.add(Material.IRON_AXE);
        interdits.add(Material.IRON_BOOTS);
        interdits.add(Material.IRON_CHESTPLATE);
        interdits.add(Material.IRON_HELMET);
        interdits.add(Material.IRON_HOE);
        interdits.add(Material.IRON_LEGGINGS);
        interdits.add(Material.IRON_PICKAXE);
        interdits.add(Material.IRON_SPADE);
        interdits.add(Material.IRON_SWORD);
        if (interdits.contains(e.getRecipe().getResult().getType())) {
            if (!e.getWhoClicked().isOp()) e.setCancelled(true);
            ((Player) e.getWhoClicked()).sendMessage(Main.getTAG() + ChatColor.RED + "Interdit :(");
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
            main.addToBalance(p.getName(), e.getItemDrop().getItemStack().getAmount() * 5);
            p.sendMessage(Main.getTAG() + ChatColor.DARK_GREEN + "+" + ChatColor.RESET + e.getItemDrop().getItemStack().getAmount() * 5 + "ƒ pour la vente");
            e.getItemDrop().remove();
            return;
        }
        ApplicableRegionSet set = WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation());
        if (set.allows(FLAG_ARENE)) {
            e.setCancelled(true);
            p.sendMessage(Main.getTAG() + "Pour ta sécurité, le drop d'item est interdit");
            p.sendMessage(Main.getTAG() + "Pour envoyer un objet, tu peux faire " + ChatColor.ITALIC + "/sendto <pseudo> [quantité]" + ChatColor.RESET + " ou sortir de l'arêne");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlaceTNT(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();
        if (!b.getType().equals(Material.TNT))
            return;


        ApplicableRegionSet set = WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(b.getLocation());
        if (set.allows(FLAG_ARENE)) {
            b.setType(Material.AIR);
            b.getWorld().spawn(b.getLocation().add(new Vector(0, 1, 0)), TNTPrimed.class);
        }
        e.setCancelled(true);
        p.getInventory().removeItem(new ItemStack(Material.TNT, 1));

    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onRespawn(PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        FakeDragon.setStatus(p,  null, -1);
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
        if (!set.allows(FLAG_ARENE)) {
            return;
        }
        e.setDroppedExp(v.getTotalExperience() % 300);
        Player d = v.getKiller();
        if (d == null) return;
        if (!d.isOnline()) return;

        if (killstreaks.containsKey(d.getName()))
            killstreaks.put(d.getName(), killstreaks.get(d.getName()) + 1);
        else
            killstreaks.put(d.getName(), 1);

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
                FakeDragon.setStatus(d, ChatColor.DARK_GREEN + congrats.get(statsa.getStreak()), -1);
            main.addToBalance(d.getName(), gain);

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

    @EventHandler
    public void onGMChange(PlayerGameModeChangeEvent e) {
        if (e.getNewGameMode().equals(GameMode.CREATIVE))
            if (!e.getPlayer().hasPermission("essentials.kick")) {
                main.broadcastToAdmins(e.getPlayer().getName() + " passe en GM1");
                main.getCLogger().severe(e.getPlayer().getName() + " passe en GM1");
            }
        if (e.getPlayer().hasMetadata("NPC")) return;
        TagAPI.refreshPlayer(e.getPlayer());
    }

    @EventHandler
    public void onFlyChange(PlayerToggleFlightEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        TagAPI.refreshPlayer(e.getPlayer());
    }

    @EventHandler
    public void onTeleportPlayer(PlayerTeleportEvent e) {
        if (e.getPlayer().hasMetadata("NPC")) return;
        if (new Random().nextBoolean()) return;
        if (e.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) return;
        if (e.getPlayer().isOnline())
            TagAPI.refreshPlayer(e.getPlayer());
    }

    @EventHandler
    public void onTAGAPI(PlayerReceiveNameTagEvent e) {
        Player u = e.getNamedPlayer();
        PermissionUser p = main.PEX.getUser(u);
        String trash = ChatColor.RESET.toString();
        int junk = (16 - p.getName().length()) / 2;
        if (junk == 0) return;
        Random rand = new Random();
        List<String> pseudo = new ArrayList<>();
        for (char i : u.getName().toCharArray()) {
            pseudo.add(String.valueOf(i));
        }
        if (GameMode.CREATIVE.equals(u.getGameMode()) || u.isFlying()) {
            if (p.inGroup("admin")) {
                pseudo.add(0, ChatColor.RED.toString());
                junk--;
                trash = ChatColor.RED.toString();
            } else if (p.inGroup("modo") || p.inGroup("co-admin")) {
                junk--;
                pseudo.add(0, ChatColor.BLUE.toString());
                trash = ChatColor.BLUE.toString();
            } else if (p.getName().equals("DleoT")) {
                junk--;
                pseudo.add(0, ChatColor.GREEN.toString());
                trash = ChatColor.GREEN.toString();
            }
        }
        for (int i = 0; i < junk; i++) {
            pseudo.add(rand.nextInt(pseudo.size()), trash);
        }
        e.setTag(Joiner.on("").skipNulls().join(pseudo));
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        User c = main.ess.getUser(e.getPlayer().getName());
        if (c.isJailed()) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(Main.getTAG() + ChatColor.RED + "Tu es jail pour " + DateUtil.formatDateDiff(c.getJailTimeout()));
            return;
        }
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
    public void onAsyncChat(AsyncPlayerChatEvent e) {

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
                i.sendMessage(Main.getTAG() + "Pour devenir " + ChatColor.GOLD + "VIP" + ChatColor.RESET + ", il suffit de te rendre sur :" + ChatColor.DARK_GREEN + " http://www.minefight.fr/boutique-en-ligne");
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
                                i.sendMessage(Main.getTAG() + "VoteKick " + mots[1] + " | Y:" + main.votekick.get(mots[1]).getVotes());
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
                // @TODO: rework this
                e.setCancelled(true);
                return;
            case "!help":
                i.sendMessage(Main.getTAG() + "Commandes disponibles :");
                //i.sendMessage("!classement");
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
        String[] insultes = new String[]{"gueule", "tg", "connard", "salop", "pute", "enfoiré", "salope", "connasse", "asshole", "nique", "faire foutre", "bâtard", "batard", "bite", "encul", "salaud"};
        for (String insulte : insultes) {
            if (e.getMessage().contains(insulte))
                e.getPlayer().damage(2);
        }
    }

    @EventHandler
    public void onJoinMessage(PlayerJoinEvent e) {
        e.setJoinMessage(ChatColor.GREEN + "+ " + ChatColor.GRAY + e.getPlayer().getDisplayName());
    }

    @EventHandler
    public void onKickMessage(PlayerKickEvent e) {
        e.setLeaveMessage(ChatColor.DARK_RED + "- " + ChatColor.GRAY + e.getPlayer().getDisplayName());
    }

    @EventHandler
    public void onQuitMessage(PlayerQuitEvent e) {
        e.setQuitMessage(ChatColor.RED + "- " + ChatColor.GRAY + e.getPlayer().getDisplayName());
    }

    @EventHandler
    public void onMoveDieOnLeave(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        ApplicableRegionSet AR_old = WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(e.getFrom());
        ApplicableRegionSet AR_new = WGBukkit.getRegionManager(p.getWorld()).getApplicableRegions(e.getTo());
        if (AR_old.allows(FLAG_DIE_ON_LEAVE) && !AR_new.allows(FLAG_DIE_ON_LEAVE))
            e.getPlayer().damage(e.getPlayer().getMaxHealth() * 2);
    }
}