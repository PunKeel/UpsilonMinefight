package fr.PunKeel.Upsilon;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.google.common.base.Joiner;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
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
    Set<String> already = new HashSet<>();
    HashSet<Material> craft_interdit = new HashSet<>();
    private Main main;
    private ItemStack emerald;

    public MoneyListener(Main m) {
        main = m;
        emerald = m.nameItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Menu principal", ChatColor.GRAY + "(Clic droit pour ouvrir)");

        craft_interdit.add(Material.GOLD_AXE);
        craft_interdit.add(Material.GOLD_BOOTS);
        craft_interdit.add(Material.GOLD_CHESTPLATE);
        craft_interdit.add(Material.GOLD_HELMET);
        craft_interdit.add(Material.GOLD_HOE);
        craft_interdit.add(Material.GOLD_LEGGINGS);
        craft_interdit.add(Material.GOLD_PICKAXE);
        craft_interdit.add(Material.GOLDEN_CARROT);
        craft_interdit.add(Material.GOLDEN_APPLE);
        craft_interdit.add(Material.GOLD_SPADE);
        craft_interdit.add(Material.GOLD_SWORD);
        craft_interdit.add(Material.IRON_AXE);
        craft_interdit.add(Material.IRON_BOOTS);
        craft_interdit.add(Material.IRON_CHESTPLATE);
        craft_interdit.add(Material.IRON_HELMET);
        craft_interdit.add(Material.IRON_HOE);
        craft_interdit.add(Material.IRON_LEGGINGS);
        craft_interdit.add(Material.IRON_PICKAXE);
        craft_interdit.add(Material.IRON_SPADE);
        craft_interdit.add(Material.IRON_SWORD);
        craft_interdit.add(Material.DIAMOND_AXE);
        craft_interdit.add(Material.DIAMOND_BOOTS);
        craft_interdit.add(Material.DIAMOND_CHESTPLATE);
        craft_interdit.add(Material.DIAMOND_HELMET);
        craft_interdit.add(Material.DIAMOND_HOE);
        craft_interdit.add(Material.DIAMOND_LEGGINGS);
        craft_interdit.add(Material.DIAMOND_PICKAXE);
        craft_interdit.add(Material.DIAMOND_SPADE);
        craft_interdit.add(Material.DIAMOND_SWORD);
        craft_interdit.add(Material.LEATHER_BOOTS);
        craft_interdit.add(Material.LEATHER_CHESTPLATE);
        craft_interdit.add(Material.LEATHER_HELMET);
        craft_interdit.add(Material.LEATHER_LEGGINGS);
        craft_interdit.add(Material.WOOD_AXE);
        craft_interdit.add(Material.WOOD_HOE);
        craft_interdit.add(Material.WOOD_SWORD);
        craft_interdit.add(Material.WOOD_SPADE);
        craft_interdit.add(Material.STONE_AXE);
        craft_interdit.add(Material.STONE_HOE);
        craft_interdit.add(Material.STONE_SWORD);
        craft_interdit.add(Material.STONE_SPADE);


    }

    @EventHandler
    public void onAnvilChange(EntityChangeBlockEvent e) {
        if (!e.getBlock().getType().equals(Material.ANVIL)) return;
        if (!e.getTo().equals(Material.AIR)) return;
        e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if (p.isDead()) {
            Main.resetPlayer(p);
        }
        if (!p.getGameMode().equals(GameMode.SURVIVAL) && !p.hasPermission("upsilon.admin"))
            p.setGameMode(GameMode.SURVIVAL);

    }

    @EventHandler(ignoreCancelled = true)
    public void onJoinGain(PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        if ((1000 * Main.getTimestamp() - main.ess.getUser(p).getLastLogout()) >= 10) {
            if (!p.hasPermission("upsilon.bypass_joinspawn"))
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, new Runnable() {
                    @Override
                    public void run() {
                        main.teleportToWarp("spawn", p);
                    }
                });
            Main.resetPlayer(p, GameMode.ADVENTURE);
        }
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
        if (craft_interdit.contains(e.getRecipe().getResult().getType())) {
            if (!e.getWhoClicked().isOp())
                e.setCancelled(true);
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
        if (set.allows(Main.FLAG_ARENE)) {
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
        if (set.allows(Main.FLAG_ARENE)) {
            b.setType(Material.AIR);
            b.getWorld().spawn(b.getLocation().add(new Vector(0, 1, 0)), TNTPrimed.class);
        }
        e.setCancelled(true);
        p.getInventory().removeItem(new ItemStack(Material.TNT, 1));

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
                        final Player c = Bukkit.getPlayerExact(mots[1]);
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
                                Bukkit.getScheduler().runTask(main, new Runnable() {
                                    @Override
                                    public void run() {
                                        c.kickPlayer(Main.getTAG() + ChatColor.RED + "Kick par la communauté Minefight");
                                    }
                                });
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
                e.getPlayer().damage(2d);
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
        if (AR_old.allows(Main.FLAG_DIE_ON_LEAVE) && !AR_new.allows(Main.FLAG_DIE_ON_LEAVE))
            e.getPlayer().damage(2000d);
    }

    @EventHandler
    public void onFuel(PlayerInteractEvent e) {
        Block b = e.getClickedBlock();
        if (b == null)
            return;
        if (b.getType().equals(Material.FURNACE)) {
            b.setType(Material.BURNING_FURNACE);
            Furnace furnace = (Furnace) b.getState();
            Short time = Short.parseShort("10000");
            MaterialData tempData = furnace.getData();
            furnace.setData(tempData);
            furnace.update(true);
            furnace.setCookTime(time);
            furnace.setBurnTime(time);
            furnace.update(true);
            b.setData(furnace.getRawData());
        }
    }
}
