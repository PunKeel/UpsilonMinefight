package fr.ungeek.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class ShopMenu implements Listener {
	Main p;
	ArrayList<String> viewing = new ArrayList<String>();
	ArrayList<ItemStack> items_list = new ArrayList<ItemStack>();

	public ShopMenu(Main main) {
		p = main;
		items_list.add(nameItem(new ItemStack(Material.DIAMOND_PICKAXE), "Pioche", "10$", "ou plus"));
	}

	ItemStack nameItem(ItemStack i, String name, String lore1, String lore2) {
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
		ArrayList<String> lore = new ArrayList<String>();
		lore.add(lore1);
		lore.add(lore2);
		im.setLore(lore);
		i.setItemMeta(im);
		return i;
	}

	public void print(Object o) {
		System.out.println(o);
	}

	public void openMenuForPlayer(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9 * (int) Math.ceil((double) items_list.size() / (double) 9), "Menu > Shop");
		viewing.add(p.getName());
		for (ItemStack i : items_list) {
			i.setAmount(64);
			inv.addItem(i);
		}
		p.openInventory(inv);
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		ItemStack[] inv = e.getView().getTopInventory().getContents();
		if (!viewing.contains(p.getName())) return;
		viewing.remove(p.getName());
		HashMap<ItemStack, Integer> restant = new HashMap<ItemStack, Integer>();
		for (ItemStack i : inv) {
			if (i == null) continue;
			int amount = i.getAmount();
			i.setAmount(1);
			print(i);
			if (restant.containsKey(i)) {
				restant.put(i, restant.get(i) + amount);
				print("Re");
			} else {
				restant.put(i, amount);
				print("new");
			}
		}

		for (int i = 0; i < items_list.size(); i++) {
			ItemStack is = items_list.get(i);
			is.setAmount(1);
			int taken;
			print(restant);
			print(is);
			if (restant.containsKey(is)) {
				taken = 64 - restant.get(is);
				print("rre");
			} else {
				taken = 64;
				print("nre");
			}
			print(taken);
			if (taken > 0) {
				p.sendMessage("Pris " + taken + " * " + items_list.get(i).getType().toString());
			}
		}
	}
}
