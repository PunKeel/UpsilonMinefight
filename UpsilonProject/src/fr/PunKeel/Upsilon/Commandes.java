package fr.PunKeel.Upsilon;

import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * User: PunKeel
 * Date: 5/13/13
 * Time: 9:06 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Commandes {
    private Main main;
    private HashMap<String, Integer> demandes_GM = new HashMap<>();

    public Commandes(Main main) {
        this.main = main;
    }

    @CommandController.CommandHandler(name = "upsilon")
    public void onUpsilon(CommandSender cs, String[] args) {
        cs.sendMessage("Usage : /ups [events,open,save,reload]");
    }

    @CommandController.CommandHandler(name = "focus")
    public void onFocus(Player cs, String[] args) {
        if (args.length == 1) {
            main.B.setFocus(cs, args[0]);
        } else {
            main.B.setFocus(cs, null);
        }
    }

    @CommandController.SubCommandHandler(parent = "upsilon", name = "events", permission = "upsilon.admin.event")
    public void onUpsilonEvents(CommandSender cs, String[] args) {
        if (args.length != 4) {
            cs.sendMessage("Usage : /ups events <status> <event> <broadcast>");
            cs.sendMessage("Usage : /ups events <on,off> <nom> <on/off>");
            HashMap<String, Boolean> events = main.event_menu.getWarps();
            for (String n : events.keySet()) {
                cs.sendMessage(n + " : " + events.get(n).toString());
            }
        } else {
            Boolean enable = args[1].equalsIgnoreCase("on");
            String event = args[2];
            Boolean broadcast = args[3].equalsIgnoreCase("on");
            Boolean success = main.event_menu.changeState(event, enable);
            if (!success) {
                cs.sendMessage("Event inexistant, sale noob");
            } else {
                main.broadcastToAdmins(ChatColor.GRAY + "<" + cs.getName() + "> Event " + event + " mis " + (enable ? "on" : "off"));
                cs.sendMessage("État changé :)");
                if (broadcast) {
                    if (enable) {
                        Bukkit.broadcastMessage(Main.getTAG() + ChatColor.DARK_GREEN + "Event " + event + " activé !");
                    } else {
                        Bukkit.broadcastMessage(Main.getTAG() + ChatColor.DARK_RED + "Event " + event + " désactivé !");
                    }
                }
            }
        }
    }

    @CommandController.SubCommandHandler(parent = "upsilon", name = "open", permission = "upsilon.admin.forceopen")
    public void onUpsilonForceOpen(CommandSender cs, String[] args) {
        if (args.length == 1) {
            cs.sendMessage("Usage /ups open <menu> [pseudo]");
            cs.sendMessage("Menus : " + StringUtils.join(MenuManager.Menus.values(), ", "));
            return;
        }
        MenuManager.Menus menu = null;
        Player p = null;
        if (MenuManager.Menus.contains(args[1].toUpperCase())) menu = MenuManager.Menus.valueOf(args[1].toUpperCase());
        if (args.length == 3) {
            p = Bukkit.getPlayer(args[2]);
        } else {
            if (cs instanceof Player) p = (Player) cs;
        }
        if (menu == null) {
            cs.sendMessage("Menu non trouvé");
            return;
        }
        if (p == null) {
            cs.sendMessage("Joueur non trouvé");
            return;
        }
        main.menu_manager.openInventory(p, menu);
        main.broadcastToAdmins(ChatColor.GRAY + "<" + cs.getName() + "> Menu " + menu + " ouvert pour " + p.getDisplayName());

    }

    @CommandController.SubCommandHandler(parent = "upsilon", name = "save", permission = "upsilon.admin.saveconfig")
    public void onUpsilonSave(CommandSender CS, String[] args) {
        main.ConfigSave();
        CS.sendMessage(Main.getTAG() + "Config sauvegardée !");
    }

    @CommandController.SubCommandHandler(parent = "upsilon", name = "reload", permission = "upsilon.admin.reloadconfig")
    public void onUpsilonReload(CommandSender CS, String[] args) {
        main.ConfigReload();
        CS.sendMessage(Main.getTAG() + "Config rechargée !");
    }

    @CommandController.CommandHandler(name = "addspawn", permission = "upsilon.arene.spawn", usage = "/addspawn <region>")
    public void onSetASpawn(Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage(Main.getTAG() + "Usage /addspawn <region>");
            return;
        }
        main.SM.addRespawn(args[0], p.getLocation());
        p.sendMessage(Main.getTAG() + "Spawn #" + main.SM.get(args[0]).length + " défini pour " + ChatColor.BOLD + args[0]);
    }

    @CommandController.CommandHandler(name = "getspawns", permission = "upsilon.arene.spawn", usage = "/getspawns <region>")
    public void onGetSpawns(Player p, String[] args) {
        if (args.length != 1) {
            p.sendMessage(Main.getTAG() + "Usage /getspawns <region>");
            return;
        }
        SLocation[] spawns = main.SM.get(args[0]);
        if (spawns == null) {
            p.sendMessage(Main.getTAG() + "Aucun spawn pour cette région");
            return;
        }
        p.sendMessage(Main.getTAG() + "Il y a " + spawns.length + " spawn(s) pour cette région");
    }

    @CommandController.CommandHandler(name = "delspawn", permission = "upsilon.arene.spawn", usage = "/delspawn <region> <id>")
    public void onDelSpawn(Player p, String[] args) {
        if (args.length != 2) {
            p.sendMessage(Main.getTAG() + "Usage /delspawn <region> <id>");
            return;
        }
        SLocation[] spawns = main.SM.get(args[0]);
        if (spawns == null) {
            p.sendMessage(Main.getTAG() + "Aucun spawn pour cette région");
            return;
        }
        int id = Integer.parseInt(args[1]);
        if (id < 0) {
            p.sendMessage(Main.getTAG() + id + "< 0 ?");
            return;
        }
        if (spawns.length < id) {
            p.sendMessage(Main.getTAG() + " Il n'y a que " + spawns.length + " spawns !");
            return;
        }
        main.SM.removeSpawn(args[0], id - 1);
    }

    @CommandController.CommandHandler(name = "tpspawn", permission = "upsilon.arene.spawn", usage = "/tpspawn <region> [id]")
    public void onTPSPawn(Player p, String[] args) {
        if (args.length < 1 || args.length > 2) {
            p.sendMessage(Main.getTAG() + "Usage /tpspawn <region> [id]");
            return;
        }
        String region = args[0];
        int id;
        SLocation[] spawns = main.SM.get(args[0]);
        if (spawns == null) {
            p.sendMessage(Main.getTAG() + "Aucun spawn pour cette région");
            return;
        }
        if (args.length == 2) {
            id = Integer.parseInt(args[1]);
            if (id < 0) {
                p.sendMessage(Main.getTAG() + id + " <0 ?");
                return;
            }
            if (spawns.length < id) {
                p.sendMessage(Main.getTAG() + " Il n'y a que " + spawns.length + " spawns !");
                return;
            }
        } else {
            id = Main.rnd.nextInt(spawns.length) + 1;
        }
        p.sendMessage(Main.getTAG() + "Téléportation à " + region + "#" + id);
        p.teleport(spawns[id - 1].toLocation());

    }

    @CommandController.CommandHandler(name = "swarp", permission = "upsilon.swarp", usage = "/swarp <to> [player]")
    public void onSwarp(CommandSender cs, String[] args) {
        if (args.length != 1 && args.length != 2) {
            cs.sendMessage(Main.getTAG() + "Usage /swarp <to> [player]");
            return;
        }
        String destination = args[0];
        String player = (args.length == 2) ? args[1] : cs.getName();
        Player d = Bukkit.getPlayerExact(player);
        if (d == null) {
            cs.sendMessage(Main.getTAG() + "Joueur non trouvé :(");
            return;
        }
        main.teleportToWarp(destination, d);

    }

    @CommandController.CommandHandler(name = "sendto", usage = "/sendto <player> [qty]")
    public void onGiveTo(Player p, String[] args) {
        if (args.length != 1 && args.length != 2) {
            p.sendMessage(Main.getTAG() + "Usage /sendto <pseudo> [qty]");
            return;
        }
        String player = args[0];
        int qty = 1;
        if (args.length == 2) {
            qty = Math.abs(Integer.parseInt(args[1]));
        }
        Player d = Bukkit.getPlayerExact(player);
        if (d == null) {
            p.sendMessage(Main.getTAG() + "Joueur non trouvé");
            return;
        }
        if (d.getName().equalsIgnoreCase(p.getName())) {
            p.sendMessage(Main.getTAG() + "Item dupliqué ! " + ChatColor.DARK_GRAY + "Just joking");
            return;
        }
        if (p.getItemInHand().getAmount() == 0) {
            p.sendMessage(Main.getTAG() + "Tu n'as aucun item en main à lui donner");
            return;
        }
        if (p.getItemInHand().getAmount() < qty) {
            p.sendMessage(Main.getTAG() + "Tu n'as pas " + qty + " fois l'item en main");
            return;
        }
        ItemStack IS = p.getItemInHand().clone();
        IS.setAmount(qty);
        p.getInventory().removeItem(IS);
        d.getInventory().addItem(IS);
        p.updateInventory();
        String display = qty + "*" + String.valueOf(IS.getType());
        if (IS.getDurability() != 0) display = display + ":" + IS.getDurability();
        if (!IS.getEnchantments().isEmpty()) {
            display = display + " (";
            for (Enchantment E : IS.getEnchantments().keySet()) {
                if (!display.substring(display.length() - 1).equalsIgnoreCase("(")) display = display + ", ";
                display = display + E.getName() + ":" + IS.getEnchantmentLevel(E);
            }
            display = display + ")";
        }
        p.sendMessage(Main.getTAG() + "Item " + display + " envoyé à " + d.getName());
        d.sendMessage(Main.getTAG() + "Item " + display + " reçu de " + p.getName());
        main.getCLogger().info(p.getName() + " a envoyé " + display + " to " + d.getName());

    }

    /*	@CommandController.CommandHandler(name = "vplkdj", permission = "upsilon.vplkdj", usage_chrono = "/vplkdj <player>")
        public void onvplkdj(CommandSender cs, String[] args) {
            if (args.length != 1) {
                cs.sendMessage(main.getTAG() + "Usage /vplkdj <player>");
                return;
            }
            String player = args[1];
        } */

    @CommandController.CommandHandler(name = "askgm", usage = "/askgm <player> ou /askgm <mode>")
    public void onGamemodeAsk(CommandSender cs, String[] args) {
        if (args.length != 1) {
            cs.sendMessage(Main.getTAG() + "Usage /askgm <player> ou /askgm <mode>");
            return;
        }
        if (cs.hasPermission("upsilon.admin.gma")) {
            String player = args[0].toLowerCase();
            if (demandes_GM.containsKey(player)) {
                Player cible = Bukkit.getPlayer(player);
                if (cible != null && cible.isOnline()) {
                    cible.setGameMode(GameMode.getByValue(demandes_GM.get(player)));
                    main.broadcastToAdmins(ChatColor.RED + player + " mis en GM " + demandes_GM.get(player) + " par " + cs.getName());
                    main.getCLogger().info(cs.getName() + " autorise  GM" + demandes_GM.get(player).toString() + " a " + player);
                    demandes_GM.remove(player);
                } else {
                    cs.sendMessage(Main.getTAG() + "Joueur non trouvée");
                }
            } else {
                cs.sendMessage(Main.getTAG() + player + " ne demande pas à changer de GM");
            }
        } else if (cs.hasPermission("upsilon.gma")) {
            GameMode new_one = GameMode.getByValue(Integer.parseInt(args[0]));
            demandes_GM.put(cs.getName().toLowerCase(), new_one.getValue());
            cs.sendMessage(Main.getTAG() + "GM demandé");
            main.broadcastToAdmins(ChatColor.RED + cs.getName() + " demande GM " + new_one.toString() + ChatColor.ITALIC + " /askgm " + cs.getName() + ChatColor.RESET + " pour accepter");

            main.getCLogger().info(cs.getName() + " demande le GM" + new_one.toString());

        } else {
            cs.sendMessage("Pas le droit");
        }
    }

    @CommandController.CommandHandler(name = "setblock", permission = "upsilon.setblock", usage = "/setblock <world> <x,y,z;x,y,z;x,y,z> <block>")
    public void onSetBlock(CommandSender cs, String[] args) {
        Chunk c;
        if (args.length != 3) {
            main.getLogger().info("Erreur /setspawn " + Arrays.toString(args) + " - nb arguments incorrect");
            return;
        }
        String world = args[0];
        String pos = args[1];
        String pattern = args[2];
        World W = Bukkit.getWorld(world);
        List<Location> locs = new ArrayList<>();
        if (W == null) {
            main.getLogger().severe("Monde " + world + " introuvable, commande /setblock " + Arrays.toString(args));
            return;
        }
        if (pos.contains(";")) {
            String[] coord = pos.split(";");
            for (String o : coord) {
                String[] t = o.split(",");
                if (t.length != 3)
                    continue;
                locs.add(new Location(W, Integer.valueOf(t[0]), Integer.valueOf(t[1]), Integer.valueOf(t[2])));
            }
        } else {
            String[] t = pos.split(",");
            if (t.length != 3)
                return;
            locs.add(new Location(W, Integer.valueOf(t[0]), Integer.valueOf(t[1]), Integer.valueOf(t[2])));
        }
        BaseBlock bb = null;
        try {
            bb = main.WE.getWorldEdit().getBlock(new fakeLocalPlayer(main.WE.getServerInterface(), BukkitUtil.getLocalWorld(W)), pattern, true, true);
        } catch (UnknownItemException | DisallowedItemException e) {
            main.getLogger().info("Item non reconnu/interdit : " + pattern);
        }
        if (bb == null) return;
        for (Location l : locs) {
            c = l.getWorld().getChunkAt(l);
            if (!c.isLoaded())
                c.load();
            l.getBlock().setTypeIdAndData(bb.getType(), (byte) bb.getData(), true);

        }

    }

    @CommandController.CommandHandler(name = "cheat", permission = "upsilon.cheat", usage = "/cheat <player>")
    public void onCheatTest(CommandSender cs, String[] args) {
        if (args.length != 1) {
            cs.sendMessage(Main.getTAG() + "Usage : /cheat <player>");
            return;
        }
        String name = args[0];
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            cs.sendMessage(Main.getTAG() + "Joueur " + name + " non trouvé");
            return;
        }
        main.AC.testPlayer(p);
        cs.sendMessage(Main.getTAG() + "Test forcefield/bowaimbot lancé sur " + p.getName());
    }

    @CommandController.CommandHandler(name = "vector", permission = "upsilon.vector", usage = "/vector <x> <y> <z> <player>")
    public void onVector(CommandSender cs, String[] args) {
        if (args.length != 4) {
            cs.sendMessage(Main.getTAG() + "Usage : /vector <x> <y> <z> <player>");
            return;
        }
        Float x = Float.valueOf(args[0]);
        Float y = Float.valueOf(args[1]);
        Float z = Float.valueOf(args[2]);
        String name = args[3];
        Player p = Bukkit.getPlayer(name);
        if (p == null) {
            cs.sendMessage(Main.getTAG() + "Joueur " + name + " non trouvé");
            return;
        }
        p.setVelocity(p.getVelocity().add(new Vector(x, y, z)));
    }

    @CommandController.CommandHandler(name = "fakeblock", permission = "upsilon.fakeblock", usage = "/fakeblock <world> <x,y,z;x,y,z;x,y,z> <block> <player>")
    public void onFakeBlock(CommandSender cs, String[] args) {
        if (args.length != 4) {

            main.getLogger().info("Erreur /fakeblock " + Arrays.toString(args) + " - nb arguments incorrect");
            return;
        }
        String world = args[0];
        String pos = args[1];
        String pattern = args[2];
        String player = args[3];
        Player p = Bukkit.getPlayer(player);
        if (p == null)
            return;
        if (!p.isOnline())
            return;
        World W = Bukkit.getWorld(world);
        List<Location> locs = new ArrayList<>();
        if (W == null) {
            main.getLogger().severe("Monde " + world + " introuvable, commande /fakeblock " + Arrays.toString(args));
            return;
        }
        if (pos.contains(";")) {
            String[] coord = pos.split(";");
            for (String o : coord) {
                String[] t = o.split(",");
                if (t.length != 3)
                    continue;
                locs.add(new Location(W, Integer.valueOf(t[0]), Integer.valueOf(t[1]), Integer.valueOf(t[2])));
            }
        } else {
            String[] t = pos.split(",");
            if (t.length != 3)
                return;
            locs.add(new Location(W, Integer.valueOf(t[0]), Integer.valueOf(t[1]), Integer.valueOf(t[2])));
        }
        BaseBlock bb = null;
        try {
            bb = main.WE.getWorldEdit().getBlock(new fakeLocalPlayer(main.WE.getServerInterface(), BukkitUtil.getLocalWorld(W)), pattern, true, true);
        } catch (UnknownItemException | DisallowedItemException e) {
            main.getLogger().info("Item non reconnu/interdit : " + pattern);
        }
        if (bb == null) return;
        for (Location l : locs)
            p.sendBlockChange(l, bb.getType(), (byte) bb.getData());
    }
}

