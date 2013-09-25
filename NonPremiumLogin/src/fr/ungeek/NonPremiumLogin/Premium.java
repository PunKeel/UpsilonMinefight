package fr.ungeek.NonPremiumLogin;

import org.bukkit.craftbukkit.libs.com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Scanner;

/**
 * + * User: PunKeel
 * + * Date: 6/17/13
 * + * Time: 3:27 PM
 * + * May be open-source & be sold (by PunKeel, of course !)
 * +
 */
class Premium {
    private HashMap<String, Boolean> premiums = new HashMap<>();
    private Main main;

    public Premium(Main m) {
        main = m;
    }

    public void loadPremiums() {
        try {
            Scanner in = new Scanner(new FileReader("premium.db"));
            Gson gson = new Gson();
            premiums = gson.fromJson(in.nextLine(), premiums.getClass());
            if (premiums == null)
                premiums = new HashMap<>();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void savePremiums() {
        try {
            FileWriter fstream = new FileWriter("premium.db");
            BufferedWriter out = new BufferedWriter(fstream);
            Gson gson = new Gson();
            out.write(gson.toJson(premiums));
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isPremium(String name) {
        name = name.toLowerCase();
        if (premiums.containsKey(name)) {
            return premiums.get(name);
        } else {
            System.out.println("Checking premium for " + name);
            try {
                URL url = new URL("https://minecraft.net/haspaid.jsp?user=" + URLEncoder.encode(name, "UTF-8"));
                HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setConnectTimeout(1000);
                huc.setReadTimeout(1000);
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(huc.getInputStream()));
                String s1 = bufferedreader.readLine();
                if (s1.equalsIgnoreCase("true")) {
                    System.out.println(name + " is premium");
                    premiums.put(name, true);
                    return true;
                }
            } catch (IOException e) {
                e.printStackTrace();
                return true;
            }
            System.out.println(name + " is NOT premium");
            premiums.put(name, false);
            return false;
        }
    }
}