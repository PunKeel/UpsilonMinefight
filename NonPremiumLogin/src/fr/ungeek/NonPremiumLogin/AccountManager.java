package fr.ungeek.NonPremiumLogin;

import org.bukkit.craftbukkit.libs.com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;

/**
 * User: PunKeel
 * Date: 6/15/13
 * Time: 5:10 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
class AccountManager {
    private Gson gson = new Gson();
    private HashMap<String, String> comptes = new HashMap<>();
    private Main main;

    public AccountManager(Main main) {
        this.main = main;
    }

    private static String encryptPassword(String name, String password) {
        String sha1 = "";
        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update((password + "|$PunKeel$" + name).getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public boolean isRegistered(String name) {
        name = name.toLowerCase();
        return comptes.containsKey(name);
    }

    public void loadAccounts() {
        comptes = gson.fromJson((String) main.getConfig().get("comptes"), comptes.getClass());
        if (comptes == null) {
            comptes = new HashMap<>();
        }
    }

    public void saveAccounts() {
        main.getConfig().set("comptes", gson.toJson(comptes));
        main.saveConfig();
    }

    public boolean checkPassword(String name, String password) {
        name = name.toLowerCase();
        return isRegistered(name) && encryptPassword(name, password).equals(comptes.get(name));
    }

    public void setPassword(String name, String password) {
        name = name.toLowerCase();
        comptes.put(name, encryptPassword(name, password));
    }
}