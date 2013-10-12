package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 5/21/13
 * Time: 4:56 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Chronos implements Listener {
    private HashMap<String, HashMap<String, Integer>> chronoHashMap = new HashMap<>();
    private HashMap<String, HashMap<String, Location>> checkpointHashMap = new HashMap<>();
    private Main main;
    private String usage_chrono;
    private String usage_checkpoint;

    public Chronos(Main main) {
        this.main = main;
        usage_chrono = "Usage : /chrono <start,stop> <name> <player> <tell(y/n)>";

        usage_checkpoint = "Usage : /checkpoint <set,reset,tp> <name> <player>>";
    }

    @CommandController.CommandHandler(name = "checkpoint", permission = "upsilon.checkpoint")
    public void onCheckpoint(CommandSender cs, String[] args) {
        cs.sendMessage(Main.getTAG() + usage_checkpoint);
    }

    @CommandController.SubCommandHandler(parent = "checkpoint", name = "set", permission = "upsilon.checkpoint")
    public void onCheckPointSet(CommandSender cs, String[] args) {
        if (args.length != 3) {
            cs.sendMessage(Main.getTAG() + usage_checkpoint);
            return;
        }
        String name = args[1];
        String joueur = args[2];
        Player p = Bukkit.getPlayer(joueur);
        if (p == null) return;
        if (!checkpointHashMap.containsKey(joueur))
            checkpointHashMap.put(joueur, new HashMap<String, Location>());

        checkpointHashMap.get(joueur).put(name, p.getLocation());


    }

    @CommandController.SubCommandHandler(parent = "checkpoint", name = "reset", permission = "upsilon.checkpoint")
    public void onCheckPointReset(CommandSender cs, String[] args) {
        if (args.length != 3) {
            cs.sendMessage(Main.getTAG() + usage_checkpoint);
            return;
        }
        String name = args[1];
        String joueur = args[2];
        Player p = Bukkit.getPlayer(joueur);
        if (p == null) return;
        if (checkpointHashMap.containsKey(joueur)) {
            checkpointHashMap.get(joueur).remove(name);
        }
    }

    @CommandController.SubCommandHandler(parent = "checkpoint", name = "tp", permission = "upsilon.checkpoint")
    public void onCheckPointTP(CommandSender cs, String[] args) {
        if (args.length != 3) {
            cs.sendMessage(Main.getTAG() + usage_checkpoint);
            return;
        }
        String name = args[1];
        String joueur = args[2];
        Player p = Bukkit.getPlayer(joueur);
        if (p == null) return;
        joueur = p.getName();
        if (checkpointHashMap.containsKey(joueur)) {
            if (checkpointHashMap.get(joueur).containsKey(name)) {
                p.teleport(checkpointHashMap.get(joueur).get(name));
            }
        }


    }

    @CommandController.CommandHandler(name = "chrono", permission = "upsilon.chrono")
    public void onChrono(CommandSender cs, String[] args) {
        cs.sendMessage(Main.getTAG() + usage_chrono);
    }

    @CommandController.SubCommandHandler(parent = "chrono", name = "start", permission = "upsilon.chrono")
    public void onChronoStart(CommandSender cs, String[] args) {
        if (args.length != 4) {
            cs.sendMessage(Main.getTAG() + usage_chrono);
            return;
        }
        String name = args[1];
        String pseudo = args[2];
        Player p = Bukkit.getPlayer(pseudo);
        boolean tell = (args[3].equalsIgnoreCase("y"));
        if (p == null) return;
        if (tell)
            if (chronoHashMap.containsKey(pseudo)) {
                p.sendMessage(Main.getTAG() + "Chronomètre remis à zéro");
            } else {
                p.sendMessage(Main.getTAG() + "Chronomètre démarré ! Fonce !");
            }
        if (!chronoHashMap.containsKey(name))
            chronoHashMap.put(name, new HashMap<String, Integer>());
        chronoHashMap.get(name).put(pseudo, Main.getTimestamp());
        //main.getLog().log(Level.ALL, "chrono start " + p);
    }

    @CommandController.SubCommandHandler(parent = "chrono", name = "stop", permission = "upsilon.chrono")
    public void onChronoStop(CommandSender cs, String[] args) {
        if (args.length != 4) {
            cs.sendMessage(Main.getTAG() + usage_chrono);
            return;
        }
        String name = args[1];
        String pseudo = args[2];
        boolean tell = (args[3].equalsIgnoreCase("y"));
        Player p = Bukkit.getPlayer(pseudo);
        if (p == null) return;
        if (!chronoHashMap.containsKey(name)) {
            chronoHashMap.put(name, new HashMap<String, Integer>());
            return;
        }
        if (!chronoHashMap.get(name).containsKey(pseudo)) return;
        int temps = Main.getTimestamp() - chronoHashMap.get(name).get(pseudo);
        if (tell)
            p.sendMessage(Main.getTAG() + "Chronomètre terminé après " + temps + " seconde" + ((temps > 1) ? "s" : ""));
        else
            main.broadcastToAdmins(Main.getTAG() + ChatColor.GRAY + "Chrono fini pour " + pseudo + " après " + temps + "seconde" + ((temps > 1) ? "s" : ""));
        chronoHashMap.get(name).remove(pseudo);
    }
}
