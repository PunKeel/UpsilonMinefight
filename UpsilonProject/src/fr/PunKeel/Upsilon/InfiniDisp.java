package fr.PunKeel.Upsilon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.block.Dropper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;

/**
 * User: PunKeel
 * Date: 5/23/13
 * Time: 1:19 AM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class InfiniDisp implements Listener {
    HashSet<String> coordonnees = new HashSet<>();
    private Main main;

    public InfiniDisp(Main main) {
        this.main = main;
    }

    @CommandController.CommandHandler(name = "infinidisp", permission = "upsilon.infinidisp", usage = "/infinidisp")
    public void onInfinidisp(Player p, String[] args) {
        Block b = p.getTargetBlock(null, 50);
        if (b == null || !(b.getType().equals(Material.DISPENSER) || b.getType().equals(Material.DROPPER))) {
            p.sendMessage(Main.getTAG() + "Tu regardes un dispenser ou dropper, au moins ?");
            return;
        }
        System.out.println("#DEBUG_PK_" + b.getWorld());
        String SL = getLocation(b);
        if (coordonnees.contains(SL)) {
            p.sendMessage(Main.getTAG() + "InfiniDisp : " + ChatColor.DARK_RED + "OFF");
            coordonnees.remove(SL);
        } else {
            p.sendMessage(Main.getTAG() + "InfiniDisp : " + ChatColor.DARK_GREEN + "ON");
            coordonnees.add(SL);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (main.isAdmin(p)) return;
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            Block b = e.getClickedBlock();
            if (b.getType().equals(Material.BEACON)) {
                e.setCancelled(true);
                return;
            }
            if (b.getType().equals(Material.DISPENSER) || b.getType().equals(Material.DROPPER)) {
                if (coordonnees.contains(getLocation(b))) {
                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onDispense(BlockDispenseEvent e) {
        Block b = e.getBlock();
        String SL = getLocation(b);
        if (!coordonnees.contains(SL)) return;
        Inventory i;
        if (b.getType().equals(Material.DISPENSER)) {
            i = ((Dispenser) b.getState()).getInventory();
        } else if (b.getType().equals(Material.DROPPER)) {
            i = ((Dropper) b.getState()).getInventory();
        } else {
            return;
        }
        i.addItem(e.getItem());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        coordonnees.remove(getLocation(b));
    }

    String getLocation(Block b) {
        return b.getWorld().getName() + "_" + b.getX() + "_" + b.getY() + "_" + b.getZ();
    }

}
