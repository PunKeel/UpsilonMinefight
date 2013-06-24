package fr.ungeek.Upsilon;

import org.bukkit.ChatColor;

import java.util.HashSet;

/**
 * User: PunKeel
 * Date: 6/9/13
 * Time: 5:03 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class FriendManager {
    HashSet<String> amis = new HashSet<>();
    Main main;

    public FriendManager(Main m) {
        main = m;
    }

    public boolean areFriends(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        return getStatus(a, b).equals(STATUS.FRIENDS);
    }

    public STATUS getStatus(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        if (amis.contains(a + ChatColor.RESET + b) && amis.contains(b + ChatColor.RESET + a)) return STATUS.FRIENDS;
        if (amis.contains(a + ChatColor.RESET + b) && !amis.contains(b + ChatColor.RESET + a))
            return STATUS.WAITING_REPLY;
        if (!amis.contains(a + ChatColor.RESET + b) && amis.contains(b + ChatColor.RESET + a)) return STATUS.INVITED;
        return STATUS.NULL;
    }

    public void addFriend(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        amis.add(a + ChatColor.RESET + b);
    }

    enum STATUS {
        WAITING_REPLY, FRIENDS, INVITED, NULL
    }
}
