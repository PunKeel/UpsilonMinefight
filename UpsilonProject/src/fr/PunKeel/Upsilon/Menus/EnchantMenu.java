package fr.PunKeel.Upsilon.Menus;

import fr.PunKeel.Upsilon.Main;
import fr.PunKeel.Upsilon.MenuManager;
import fr.PunKeel.Upsilon.events.MenuChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;

import java.util.Random;

/**
 * User: PunKeel
 * Date: 5/5/13
 * Time: 8:00 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class EnchantMenu implements Listener {
    Main m;
    MenuManager MM;
    int level_max = 15;

    public EnchantMenu(Main main, MenuManager MM) {
        m = main;
        this.MM = MM;
    }

    public void load_config() {
        if (m.getConfig().contains("level_max")) {
            level_max = m.getConfig().getInt("level_max", 10);
            if (level_max < 0 || level_max > 101) {
                level_max = 15;
            }
        }
    }

    public int getLevel_max() {
        return level_max;
    }

    public MenuManager.Menus getSelfMenuType() {
        return MenuManager.Menus.ENCHANTING;
    }

    @EventHandler
    public void onMenuOpen(MenuChangeEvent e) {
        if (!e.getNew_menu().equals(getSelfMenuType())) return;
        MM.current_menu.put(e.getPlayer().getName(), getSelfMenuType());
        e.getPlayer().openEnchanting(null, true);
    }

    @EventHandler
    public void onEnchant(PrepareItemEnchantEvent e) {
        Player p = e.getEnchanter();
        if (e.isCancelled()) return;
        if (!MM.current_menu.containsKey(p.getName())) return;
        if (MM.current_menu.get(p.getName()) != MenuManager.Menus.ENCHANTING) return;

        int[] levels = e.getExpLevelCostsOffered();
        Random rnd = new Random();
        levels[0] = Math.min(level_max / 2, rnd.nextInt(2 * (int) Math.sqrt(level_max)) + 1);
        levels[1] = Math.min(level_max - 1, levels[0] + rnd.nextInt(2 * (int) Math.sqrt(level_max)) + 1);
        levels[2] = Math.min(level_max - 1, levels[1] + rnd.nextInt(2 * (int) Math.sqrt(level_max)) + 1);

    }
}
