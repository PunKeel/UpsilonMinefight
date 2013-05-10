package fr.ungeek.Upsilon;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

/**
 * User: PunKeel
 * Date: 5/9/13
 * Time: 6:43 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class GimmeEmerald implements Listener {
	Main main;
	ItemStack emerald;

	public GimmeEmerald(Main m) {
		main = m;
		emerald = m.nameItem(new ItemStack(Material.EMERALD), ChatColor.GREEN + "Menu principal", ChatColor.GRAY + "(Clic droit pour ouvrir)");

	}

	@EventHandler
	public void onLogin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (!p.getInventory().containsAtLeast(emerald, 1)) {
			if (!p.getEnderChest().containsAtLeast(emerald, 1)) {
				if (p.getInventory().firstEmpty() != -1) {
					p.getInventory().addItem(emerald);
				} else {
					if (p.getEnderChest().firstEmpty() != -1) {
						p.getInventory().addItem(emerald);
					} else {
						p.sendMessage(main.getTAG() + "Ton inventaire est plein, vide le un peu et tape /menu pour avoir une émeraude ! :)");
					}
				}
			}
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (main.isAdmin(p)) return;
		if (e.getItemDrop().getItemStack().isSimilar(emerald)) e.setCancelled(true);
	}
}
