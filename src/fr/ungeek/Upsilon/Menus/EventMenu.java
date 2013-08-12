package fr.ungeek.Upsilon.Menus;

import fr.ungeek.Upsilon.Main;
import fr.ungeek.Upsilon.MenuManager;
import fr.ungeek.Upsilon.events.MenuChangeEvent;
import fr.ungeek.Upsilon.events.MenuClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class EventMenu implements Listener {
    Main m;
    MenuManager MM;
    HashMap<String, Boolean> warps = new HashMap<String, Boolean>();
    List<String> slots = new ArrayList<String>();
    Inventory inv;
    private ItemStack dac;
    private ItemStack sauta;
    private ItemStack sautb;
    private ItemStack color;
    private ItemStack lune;
    private ItemStack vitesse;
    private ItemStack skin;
    private ItemStack jump_potion;
    private ItemStack pvp;
    private ItemStack sautc;

    public EventMenu(Main main, MenuManager MM) {
        m = main;
        this.MM = MM;
        warps.put("dac", true); // echelle
        warps.put("pvp", true); // ender stone
        warps.put("saut1", true);         // popo
        warps.put("saut2", true);          // popo
        warps.put("saut3", true);          // popo
        warps.put("color", true); // laine
        warps.put("vitesse", true); // popo swift
        warps.put("skin", true); // tete de steve
        warps.put("lune", true); // ender stone

        slots.add("dac");
        slots.add("pvp");
        slots.add("saut1");
        slots.add("saut2");
        slots.add("saut3");
        slots.add("color");
        slots.add("vitesse");
        slots.add("skin");
        slots.add("lune");
        jump_potion = new ItemStack(Material.POTION);
        PotionMeta p_meta = (PotionMeta) jump_potion.getItemMeta();
        p_meta.setMainEffect(PotionEffectType.JUMP);
        jump_potion.setItemMeta(p_meta);

        ItemStack night_potion = new ItemStack(Material.POTION);
        p_meta = (PotionMeta) night_potion.getItemMeta();
        p_meta.setMainEffect(PotionEffectType.NIGHT_VISION);
        night_potion.setItemMeta(p_meta);

        inv = Bukkit.createInventory(null, 9, "Menu > Téléportation > Events");
    }

    public void generateItems() {
        String lore_off = ChatColor.DARK_RED + "Event indisponible";
        String lore_on = ChatColor.GREEN + "Cliquez pour rejoindre";

        dac = m.nameItem(new ItemStack(Material.LADDER), ChatColor.DARK_AQUA + "Event DAC", warps.get("dac") ? lore_on : lore_off);
        sauta = m.nameItem(jump_potion.clone(), ChatColor.DARK_AQUA + "Event Saut 1", warps.get("saut1") ? lore_on : lore_off);
        sautb = m.nameItem(new ItemStack(Material.GOLD_INGOT), ChatColor.DARK_AQUA + "Event Saut 2", warps.get("saut2") ? lore_on : lore_off);
        sautc = m.nameItem(new ItemStack(Material.GOLD_INGOT), ChatColor.DARK_AQUA + "Event Saut 3", warps.get("saut3") ? lore_on : lore_off);
        color = m.nameItem(new ItemStack(Material.WOOL, 1, (short) 1), ChatColor.DARK_AQUA + "Event Color", warps.get("color") ? lore_on : lore_off);
        vitesse = m.nameItem(new ItemStack(Material.POTION, 1, (short) 8258), ChatColor.DARK_AQUA + "Event Vitesse", warps.get("vitesse") ? lore_on : lore_off);
        skin = m.nameItem(new ItemStack(Material.getMaterial(397), 1, (short) 3), ChatColor.DARK_AQUA + "Event Skins", warps.get("skin") ? lore_on : lore_off);
        lune = m.nameItem(new ItemStack(Material.ENDER_STONE), ChatColor.DARK_AQUA + "La lune", warps.get("lune") ? lore_on : lore_off);
        pvp = m.nameItem(new ItemStack(Material.GOLD_SWORD), ChatColor.GOLD + "PVP", warps.get("pvp") ? lore_on : lore_off);
        inv.setItem(0, dac);
        inv.setItem(1, pvp);
        inv.setItem(2, sauta);
        inv.setItem(3, sautb);
        inv.setItem(4, sautc);
        inv.setItem(5, color);
        inv.setItem(6, vitesse);
        inv.setItem(7, skin);
        inv.setItem(8, lune);

    }

    public HashMap<String, Boolean> getWarps() {
        return warps;
    }

    public void loadWarps() {
        for (String s : warps.keySet()) {
            if (!m.getConfig().contains("events." + s)) continue;
            warps.put(s, m.getConfig().getBoolean("events." + s, false));
        }
        generateItems();
    }

    public Boolean changeState(String name, Boolean state) {
        if (!warps.containsKey(name)) {
            return false;
        } else {
            if (!warps.get(name).equals(state)) {
                warps.put(name, state);
                generateItems();
                for (String pseudo : MM.current_menu.keySet()) {
                    if (MM.current_menu.get(pseudo).equals(MenuManager.Menus.EVENTS)) {

                        Player p = Bukkit.getPlayerExact(pseudo);
                        Inventory inv = p.getOpenInventory().getTopInventory();
                        inv.setItem(0, dac);
                        inv.setItem(2, sauta);
                        inv.setItem(3, sautb);
                        inv.setItem(4, sautc);
                        inv.setItem(5, color);
                        inv.setItem(6, vitesse);
                        inv.setItem(7, skin);
                        inv.setItem(8, lune);
                        inv.setItem(1, pvp);

                    }

                }
            }
            return true;
        }
    }

    public MenuManager.Menus getSelfMenuType() {
        return MenuManager.Menus.EVENTS;
    }

    @EventHandler
    public void onMenuOpen(MenuChangeEvent e) {
        if (!e.getNew_menu().equals(getSelfMenuType())) return;
        MM.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
        e.getPlayer().openInventory(inv);
    }

    @EventHandler
    public void onMenuClick(MenuClickEvent e) {
        if (!e.getCurrent_menu().equals(getSelfMenuType())) return;
        if (slots.size() <= e.getEvent().getSlot()) return;
        String warp = String.valueOf(slots.get(e.getEvent().getSlot()));
        if (warp == null) return;
        if (warp.isEmpty()) return;
        Player p = (Player) e.getEvent().getWhoClicked();

        if (p.hasPermission("upsilon.admin.event")) {
            boolean toggle, broadcast;
            toggle = e.getEvent().isRightClick();
            broadcast = e.getEvent().isShiftClick();
            if (toggle) {
                changeState(warp, !warps.get(warp));
                m.broadcastToAdmins(ChatColor.GRAY + "<" + p.getName() + "> Event " + warp + " mis " + (warps.get(warp) ? "on" : "off"));
                if (broadcast) {
                    if (warps.get(warp)) {
                        Bukkit.broadcastMessage(Main.getTAG() + ChatColor.DARK_GREEN + "Event " + warp.toUpperCase() + " ouvert");
                    } else {
                        Bukkit.broadcastMessage(Main.getTAG() + ChatColor.DARK_RED + "Event " + warp.toUpperCase() + " fermé");
                    }
                }
                return;
            }
        }
        if (m.isAdmin(p)) {

            MM.closeInventory(p);
            m.teleportToWarp(warp, p);
            if (!warps.get(warp))
                p.sendMessage(Main.getTAG() + "Cet événement est fermé pour le moment");

        } else {
            if (warps.get(warp)) {
                MM.closeInventory(p);
                m.teleportToWarp(warp, p);
            } else
                p.sendMessage(Main.getTAG() + "Cet événement est fermé pour le moment");
        }
    }
}
