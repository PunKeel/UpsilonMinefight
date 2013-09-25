package fr.PunKeel.Upsilon.Games;

import fr.PunKeel.Upsilon.CommandController;
import fr.PunKeel.Upsilon.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * User: PunKeel
 * Date: 6/11/13
 * Time: 3:48 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Spleef implements Listener {
    private static String TAG = ChatColor.RESET + "[" + ChatColor.BLUE + "Spleef" + ChatColor.RESET + "] ";
    private static String recordman = "";
    private HashMap<String, Location> participants = new HashMap<>();
    private List<Material> mats = new ArrayList<>();
    private Boolean playing = false;
    private Boolean single_player = false;
    private Random random = new Random();
    private long debut_chrono = 0;
    private long record = (long) 0;
    private ArrayList<Location> blocs;
    private int radius = 12;
    private int centreX = -215;
    private int centreZ = 311;
    private int centreY = 61;
    private int loop = 0;
    private int countdown = 15;
    private int task_id = 0;
    private int max_joueurs = -1;
    private Main main;

    public Spleef(Main main) {
        this.main = main;
        mats.add(Material.LAPIS_ORE);
        mats.add(Material.SNOW_BLOCK);
        mats.add(Material.GOLD_ORE);
        mats.add(Material.IRON_ORE);
        mats.add(Material.REDSTONE_ORE);
        mats.add(Material.BOOKSHELF);
        mats.add(Material.WOOL);
        mats.add(Material.ICE);
        mats.add(Material.EMERALD_BLOCK);
        mats.add(Material.SOUL_SAND);
        mats.add(Material.DIAMOND_BLOCK);
        mats.add(Material.BEDROCK);
        mats.add(Material.SANDSTONE);
        mats.add(Material.BEACON);

    }

    private void startSpleef() {
        blocs = getBlocks();
        Material mat = mats.get(random.nextInt(mats.size()));
        for (Location l : blocs)
            l.getBlock().setType(mat);
        for (String joueur : participants.keySet()) {
            Bukkit.getPlayer(joueur).sendMessage(TAG + "GO !");
            Main.resetPlayer(Bukkit.getPlayer(joueur), GameMode.SURVIVAL);
        }
        playing = true;
    }

    boolean isInCircle(int x, int z) {
        return (x - centreX) * (x - centreX) + (z - centreZ) * (z - centreZ) <= (radius * radius);
    }

    ArrayList<Location> getBlocks() {
        ArrayList<Location> t = new ArrayList<>();
        for (int X = -radius; X <= radius; X++) {
            for (int Z = -radius; Z <= radius; Z++) {
                if (Math.sqrt((X * X) + (Z * Z)) > radius) continue;
                t.add(new Location(Bukkit.getWorld(Main.WORLDNAME), (double) centreX + X, (double) centreY, (double) centreZ + Z));
            }
        }
        return t;
    }

    private void stopSpleef() {
        max_joueurs = -1;
        stop_task();
        Player gagnant = null;
        playing = false;
        loop = 0;
        if (participants.size() == 1)
            gagnant = Bukkit.getPlayer((String) participants.keySet().toArray()[0]);
        for (String p : participants.keySet()) {
            if (!single_player)
                Bukkit.getPlayer(p).sendMessage(TAG + ChatColor.GREEN + "" + ChatColor.BOLD + "Bravo ! Tu as gagné.");
            Bukkit.getPlayer(p).teleport(participants.get(p));
        }
        if (gagnant != null) {
            main.broadcastToAdmins(gagnant.getName() + " gagne le spleef", false);

            if (single_player) {
                Long diff = (Main.getTimestamp() - debut_chrono);
                gagnant.sendMessage(TAG + "Ton temps : " + diff + " s");
                if (diff > record) {
                    record = diff;
                    recordman = gagnant.getName();
                }

            }
        }
        participants = new HashMap<>();
        for (Location l : blocs)
            l.getBlock().setType(Material.GLASS);
        single_player = false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (participants.containsKey(event.getPlayer().getName()))
            PlayerQuit(event.getPlayer().getName());
    }

    void start_task() {
        if (task_id != 0) stop_task();
        blocs = getBlocks();
        task_id = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(main, new Runnable() {
            public void run() {
                if ((single_player || (Main.getTimestamp() - debut_chrono) > 90) && playing) {
                    for (int i = 0; i < 10; i++)
                        if (random.nextBoolean() && random.nextBoolean()) {
                            blocs.get(random.nextInt(blocs.size() - 1)).getBlock().setType(Material.AIR);
                        }
                }
                for (String joueur : ((HashMap<String, Location>) participants.clone()).keySet()) {
                    Player p = Bukkit.getPlayer(joueur);
                    if (p == null) continue;
                    if (!p.isOnline()) continue;
                    if (p.getLocation().getY() <= centreY - .5 || !isInCircle(p.getLocation().getBlockX(), p.getLocation().getBlockZ())) {
                        PlayerQuit(joueur);
                        p.sendMessage(TAG + ChatColor.DARK_RED + "Perdu ! " + ChatColor.RESET + "Tu n'es plus dans la zone de jeu !");
                        continue;
                    }
                    Main.resetPlayer(p, GameMode.SURVIVAL);
                }
                if (!playing) {
                    if (max_joueurs == -1) {
                        if ((Main.getTimestamp() - debut_chrono) > countdown && participants.size() >= 2) {
                            startSpleef();
                        } else if (participants.size() >= 2 && loop != Main.getTimestamp()) {
                            loop = Main.getTimestamp();
                            for (String joueur : participants.keySet())
                                Bukkit.getPlayer(joueur).sendMessage(TAG + "Début dans " + (countdown - (Main.getTimestamp() - debut_chrono)) + "s");
                        }
                    } else {
                        if (max_joueurs <= participants.size()) {
                            startSpleef();
                        }
                    }

                }
            }


        }, 20L, 10L);
    }

    void stop_task() {
        Bukkit.getScheduler().cancelTask(task_id);
        task_id = 0;
    }

    void PlayerQuit(String name) {
        Player p = Bukkit.getPlayer(name);

        p.teleport(participants.get(p.getName()));
        Main.resetPlayer(p, GameMode.ADVENTURE);
        if (single_player) {
            stopSpleef();
            return;
        }
        participants.remove(name);
        if (participants.size() == 1) {
            stopSpleef();
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equals(Main.WORLDNAME) || !playing) {
            return;
        }
        Block targetBlock = event.getClickedBlock();
        Location l = targetBlock.getLocation();

        if (participants.containsKey(player.getName())) {
            if (isInCircle(l.getBlockX(), l.getBlockZ()) && l.getBlockY() == centreY) {
                targetBlock.setType(Material.AIR);
            }
        }

    }

    @CommandController.CommandHandler(name = "spleef")
    public void onSpleef(Player cs, String[] args) {
        if (playing) {
            cs.sendMessage(TAG + "Une partie est déjà en cours");
            return;
        }
        if (participants.containsKey(cs.getName())) {
            cs.sendMessage(TAG + "Tu participes déjà !");
            return;
        }
        if (task_id == 0) start_task();
        participants.put(cs.getName(), cs.getLocation());
        cs.sendMessage(TAG + "Tu es participant ! :)");
        debut_chrono = Main.getTimestamp();
        main.teleportToWarp("spleef_centre", cs);
    }

    @CommandController.SubCommandHandler(parent = "spleef", name = "start", permission = "upsilon.admin.event")
    public void onSpleefStart(Player cs, String[] args) {
        startSpleef();
        cs.sendMessage(TAG + "début du spleef");
    }

    @CommandController.SubCommandHandler(parent = "spleef", name = "stop", permission = "upsilon.admin.event")
    public void onSpleefStop(Player cs, String[] args) {
        stopSpleef();
    }

    @CommandController.SubCommandHandler(parent = "spleef", name = "reset", permission = "upsilon.admin.event")
    public void onSpleefReset(Player cs, String[] args) {
        blocs = getBlocks();
        for (Location l : blocs)
            l.getBlock().setType(Material.GLASS);
    }

    @CommandController.SubCommandHandler(parent = "spleef", name = "solo", permission = "upsilon.admin.event")
    public void onSpleefSolo(Player cs, String[] args) {
        if (playing) {
            cs.sendMessage(TAG + "Une partie est déjà en cours");
            return;
        }
        participants.put(cs.getName(), cs.getLocation());
        main.teleportToWarp("spleef_centre", cs);
        startSpleef();
        single_player = true;
        debut_chrono = Main.getTimestamp();
        start_task();
    }

    @CommandController.SubCommandHandler(parent = "spleef", name = "init", permission = "upsilon.admin.event")
    public void onSpleefInit(Player cs, String[] args) {
        if (playing) {
            cs.sendMessage(TAG + "Une partie est déjà en cours");
            return;
        }
        if (args.length != 2) {
            cs.sendMessage("Usage : /spleef init <nb joueurs>");
            return;
        }
        max_joueurs = Integer.parseInt(args[1]);
        main.broadcastToAdmins("Partie spleef avec " + max_joueurs + " joueurs :) by " + cs.getName());
    }

}
