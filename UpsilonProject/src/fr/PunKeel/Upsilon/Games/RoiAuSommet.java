package fr.PunKeel.Upsilon.Games;

import fr.PunKeel.Upsilon.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: PunKeel
 * Date: 5/11/13
 * Time: 4:21 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class RoiAuSommet implements Listener, CommandExecutor {

    List<String> participants = new ArrayList<>();
    String TAG = ChatColor.DARK_AQUA + "[" + ChatColor.DARK_GREEN + "Roi Au Sommet" + ChatColor.DARK_AQUA + "] " + ChatColor.RESET;
    ETAPES etat = ETAPES.OFF;
    Integer joueurs_max = 20;
    Main main;
    Location portail = null;
    Random rnd;

    public RoiAuSommet(Main m) {
        main = m;
        rnd = new Random();
    }

    Location getPortal() {
        if (portail == null) {
            portail = new Location(Bukkit.getWorld("world"), -274, 100, 341);
        }
        return portail;

    }

    Location getSpawn() {
        int x = -(rnd.nextInt(50) + 245);
        int y = 62;
        int z = 306;
        if (x == -245 || x == -295) {
            z = rnd.nextInt(373 - 306) + 306;
        }
        return new Location(Bukkit.getWorld("world"), x, y, z);

    }

    void broadcastParticipants(String message) {
        for (String s : participants) {
            Bukkit.getPlayerExact(s).sendMessage(TAG + message);
        }
    }

    void rejoindre(CommandSender sender) {
        if (etat.equals(ETAPES.JOIN)) {
            if (participants.contains(sender.getName())) {
                sender.sendMessage(TAG + ChatColor.DARK_GREEN + "Tu es déjà participant :)");
            } else {
                broadcastParticipants(sender.getName() + " a rejoint la partie");
                participants.add(sender.getName());
                sender.sendMessage(TAG + ChatColor.DARK_GREEN + "Tu es désormais un participant !");
                sender.sendMessage(TAG + ChatColor.DARK_GREEN + "Participants (" + participants.size() + "/" + joueurs_max + ") : ");
                for (String s : participants) {
                    sender.sendMessage(" - " + s);
                }
                if (participants.size() >= joueurs_max) {
                    debuter();
                }
            }
        } else if (etat.equals(ETAPES.STARTED)) {
            sender.sendMessage(TAG + ChatColor.DARK_RED + "Une partie est déjà en cours ! :(");
        } else if (etat.equals(ETAPES.OFF)) {
            sender.sendMessage(TAG + ChatColor.DARK_RED + "Aucune partie pour le moment, repasses plus tard ! :)");
        }
    }

    void preparePlayer(Player p) {
        if (!p.getGameMode().equals(GameMode.ADVENTURE))
            p.setGameMode(GameMode.ADVENTURE);
        p.teleport(getSpawn());
        Main.resetPlayer(p);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 1));
    }

    void debuter() {
        etat = ETAPES.STARTED;
        for (String s : participants) {
            Player p = Bukkit.getPlayerExact(s);
            p.sendMessage(TAG + "Bon jeu !");
            preparePlayer(p);
        }
    }

    void stopper() {
        etat = ETAPES.OFF;
        for (String s : participants) {
            main.teleportToWarp("spawn", Bukkit.getPlayerExact(s));
        }
        participants.clear();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if (participants.contains(p.getName())) {
            participants.remove(p.getName());
            if (participants.size() == 1) {
                stopper();
            }
        }
    }

    @EventHandler
    public void onDeath(EntityDamageEvent e) {
        if (!etat.equals(ETAPES.STARTED)) return;
        if (!e.getEntityType().equals(EntityType.PLAYER)) return;
        Player p = (Player) e.getEntity();
        if (!participants.contains(p.getName())) return;
        if (e.getDamage() >= p.getHealth()) {
            p.sendMessage(TAG + ChatColor.DARK_RED + "Tu es mort.");
            preparePlayer(p);
            e.setDamage(0);
            p.setFireTicks(0);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("roi")) return false;
        boolean senderIsAdmin = false;
        if (sender.getName().equalsIgnoreCase("dleot")) senderIsAdmin = true;
        else if (sender.isOp()) senderIsAdmin = true;
        else if (sender.hasPermission("upsilon.roi.admin")) senderIsAdmin = true;
        if (!senderIsAdmin) {
            main.broadcastToAdmins(sender.getName() + ":join");
            rejoindre(sender);
            return true;
        } else {
            if (args.length == 0) {
                sender.sendMessage(TAG + "Usage : /roi [join|init|start|stop]");
            } else {
                String fonction = args[0].toLowerCase().trim();
                if (fonction.equalsIgnoreCase("join")) {
                    rejoindre(sender);
                    return true;
                } else if (fonction.equals("init")) {
                    if (args.length != 2) {
                        sender.sendMessage(TAG + "Usage : /roi init [nombre joueurs]");
                    } else {
                        Integer joueurs = 20;
                        try {
                            joueurs = Integer.parseInt(args[1]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage(TAG + ChatColor.DARK_RED + "On dirait que " + args[1] + " n'est pas un nombre :(");
                        }
                        if (etat == ETAPES.OFF) {
                            main.broadcastToAdmins(ChatColor.DARK_GRAY + "Init[RoiAuSommet] (" + joueurs + ") par " + sender.getName());
                            joueurs_max = joueurs;
                            etat = ETAPES.JOIN;
                            sender.sendMessage(TAG + "Ouverture des participations, " + joueurs.toString() + " joueur(s)");
                        } else if (etat == ETAPES.JOIN || etat == ETAPES.STARTED) {
                            sender.sendMessage(TAG + ChatColor.DARK_RED + "Une partie est déjà en cours");
                        }
                    }
                } else if (fonction.equals("start")) {
                    if (etat == ETAPES.JOIN) {
                        main.broadcastToAdmins(TAG + ChatColor.DARK_GRAY + "Start par " + sender.getName());
                        debuter();
                        sender.sendMessage(TAG + "Lancement de la partie avec " + participants.size() + " joueurs / " + joueurs_max);
                    } else if (etat == ETAPES.OFF || etat == ETAPES.STARTED) {
                        sender.sendMessage(TAG + ChatColor.DARK_RED + "Une partie est déjà en cours [OU] pas initiée");
                    }
                } else if (fonction.equals("stop")) {
                    if (etat == ETAPES.STARTED) {
                        main.broadcastToAdmins(TAG + ChatColor.DARK_GRAY + "Stop par " + sender.getName());
                        stopper();
                        sender.sendMessage(TAG + ChatColor.DARK_RED + "Arrêt de la partie");
                    } else if (etat == ETAPES.OFF || etat == ETAPES.JOIN) {
                        sender.sendMessage(TAG + ChatColor.DARK_RED + "Aucune partie en cours");
                    }
                } else {
                    sender.sendMessage(TAG + ChatColor.DARK_RED + "commande inconnue");
                }
            }

        }

        return false;
    }

    @EventHandler
    public void onPortalWin(PlayerPortalEvent e) {
        if (etat != ETAPES.STARTED) return;
        Location loc = e.getFrom();
        if (!loc.getWorld().getName().equals("world")) return;
        if (loc.distance(getPortal()) < 4) {
            Player p = e.getPlayer();
            broadcastParticipants(ChatColor.DARK_GREEN + p.getDisplayName() + " a gagné !");
            stopper();
            e.setCancelled(true);
        }
    }

    enum ETAPES {OFF, JOIN, STARTED}

}

