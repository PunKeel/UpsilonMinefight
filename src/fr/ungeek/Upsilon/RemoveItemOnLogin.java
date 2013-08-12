package fr.ungeek.Upsilon;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * User: PunKeel
 * Date: 8/12/13
 * Time: 3:53 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class RemoveItemOnLogin {
    Set<String> already = new HashSet<>();

    public void removeFor(Player p) {

    }

    public void load(Set<String> conf) {
        already = conf;
    }
}
