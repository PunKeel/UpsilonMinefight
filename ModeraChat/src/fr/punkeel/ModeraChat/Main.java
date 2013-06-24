package fr.punkeel.ModeraChat;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: PunKeel
 * Date: 1/1/13
 * Time: 2:24 PM
 * May be open-source & be sold (by PunKeel, of course !)
 */
public class Main extends JavaPlugin implements Listener {
    HashMap<String, Integer> warnings = new HashMap<>();
    Set<String> badwords = new HashSet<>(), badlocutions = new HashSet<>(), traductions_in_jars = new HashSet<>();
    File BW_file, W_file, BL_file, lng_file;
    boolean ignore_nicknames, block_ip, block_url, block_badwords, block_capslock, block_repeated_letters, block_alternative_case, block_double_post, block_too_fast_talking, save_warnings_on_reload, block_badlocutions, locutions_block_aggressive;
    Integer warnings_before_ban = 3, min_repeated_letters = 3, too_fast_talking_delay = 400, block_repeated_times = 3, max_word_length = 0;
    Pattern ip, url, capslock, repeated_letters, alternative_case_a, alternative_case_A;
    String banCommand = "ban %nickname%";
    List<String> domainWhitelist = new ArrayList<>(), countWarningsFor = new ArrayList<>();
    HashMap<String, Long> last_message_time = new HashMap<>();
    HashMap<String, String> last_message = new HashMap<>();
    HashMap<String, String> traductions = new HashMap<>();
    Set<String> usernames = new HashSet<>();

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        saveConfig();
        loadConfig();

        getServer().getPluginManager().registerEvents(this, this);

        BW_file = new File(getDataFolder() + File.separator + "badwords.txt");
        W_file = new File(getDataFolder() + File.separator + "warnings.txt");
        BL_file = new File(getDataFolder() + File.separator + "badlocutions.txt");
        lng_file = new File(getDataFolder() + File.separator + "lang.txt");

        traductions_in_jars.add("fr");
        traductions_in_jars.add("en");

        loadbadWords();
        loadWarnings();
        loadbadLocutions();
        loadTranslation();
        try {
            MCStats metrics = new MCStats(this);
            metrics.start();
        } catch (IOException e) {
            getLogger().info("Can't mCs");
        }
        usernames.clear();
        if (!ignore_nicknames) {
            for (Player p : Bukkit.getOnlinePlayers())
                usernames.add(p.getName());
        }
    }

    @Override
    public void onDisable() {
        if (save_warnings_on_reload) {
            Joiner.MapJoiner joiner = Joiner.on("\n").withKeyValueSeparator(":!:");
            try {
                FileWriter FW = new FileWriter(new File(getDataFolder() + File.separator + "warnings.txt"));
                FW.write(joiner.join(warnings));
                FW.close();
            } catch (IOException e) {
                getLogger().severe(e.getMessage());
            }
        }
    }

    public void addWarning(Player p, String raison) {
        if (!countWarningsFor.contains(raison)) return;
        if (warnings.containsKey(p.getName())) {
            if (warnings_before_ban - warnings.get(p.getName()) <= 1) {
                Command cmd = getServer().getPluginCommand("ban");
                if (cmd != null)
                    getServer().dispatchCommand(getServer().getConsoleSender(), banCommand.replace("%nickname%", p.getName()));
                warnings.put(p.getName(), 1);
                if (last_message.containsKey(p.getName())) {
                    getLogger().info(p.getName() + " Last message : " + last_message.get(p.getName()).substring(last_message.get(p.getName()).indexOf("/>!</") + 5));
                }
            }
            warnings.put(p.getName(), warnings.get(p.getName()) + 1);
        } else {
            warnings.put(p.getName(), 1);
        }
        int avert_before_ban = warnings_before_ban - warnings.get(p.getName());
        if (avert_before_ban >= 2) {
            p.sendMessage(traductions.get("warnings_left").replace("%left%", String.valueOf(avert_before_ban)));
        } else if (avert_before_ban == 1) {
            p.sendMessage(traductions.get("last_warning"));
        }
    }

    public void loadbadWords() {
        try {
            if (!BW_file.exists()) {
                BW_file.createNewFile();
                getDataFolder().mkdirs();
            }

            FileInputStream fstream = new FileInputStream(BW_file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                badwords.add(strLine.trim().replaceAll("\\W", " ").toLowerCase());
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
        }
    }

    public void loadbadLocutions() {
        try {
            if (!BL_file.exists()) {
                BL_file.createNewFile();
                getDataFolder().mkdirs();
            }

            FileInputStream fstream = new FileInputStream(BL_file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                badlocutions.add(strLine.trim().toLowerCase());
            }
            in.close();
            fstream.close();
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
        }
    }

    boolean containsIgnoreCase(List<String> list, String str) {
        if (list == null) return false;
        if (list.contains(str)) return true;
        for (String i : list) {
            if (i.equalsIgnoreCase(str))
                return true;
        }
        return false;
    }

    public void loadConfig() {
        reloadConfig();

        block_ip = getConfig().getBoolean("block_ip", true);
        block_url = getConfig().getBoolean("block_url", true);
        block_badwords = getConfig().getBoolean("block_badwords", true);
        block_capslock = getConfig().getBoolean("block_capslock", true);
        block_repeated_letters = getConfig().getBoolean("block_repeated_letters", true);
        block_alternative_case = getConfig().getBoolean("block_alternative_case", true);
        block_double_post = getConfig().getBoolean("block_double_post", true);
        block_too_fast_talking = getConfig().getBoolean("block_too_fast_talking", true);
        block_badlocutions = getConfig().getBoolean("block_badlocutions", true);
        locutions_block_aggressive = getConfig().getBoolean("locutions_block_aggressive", false);

        warnings_before_ban = getConfig().getInt("warnings_before_ban", 3);
        min_repeated_letters = getConfig().getInt("min_repeated_letters", 3);
        too_fast_talking_delay = getConfig().getInt("too_fast_talking_delay", 400);
        block_repeated_times = getConfig().getInt("block_repeated_times", 3);

        max_word_length = getConfig().getInt("max_word_length", 0);
        getLogger().info(max_word_length.toString());

        banCommand = getConfig().getString("ban_command", "ban %nickname%");
        domainWhitelist = getConfig().getStringList("domainWhitelist");
        countWarningsFor = getConfig().getStringList("count_warnings_for");

        save_warnings_on_reload = getConfig().getBoolean("save_warnings_on_reload", true);
        ignore_nicknames = getConfig().getBoolean("ignore_nicknames", true);

        ip = Pattern.compile("(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\s*?)[\\.| ](\\s*?)(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\s*?)[\\.| ](\\s*?)(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\s*?)[\\.| ](\\s*?)(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)");
        url = Pattern.compile("([0-9a-zA-Z\\.\\-]+?)(\\s*?)\\.(\\s*?)(fr|com|org|net|me|de|be|us)");
        capslock = Pattern.compile("^[A-Z -]$");
        alternative_case_a = Pattern.compile("^([A-Z]\\W*?[a-z])*[a-zA-Z]?$");
        alternative_case_A = Pattern.compile("^([a-z]\\W*?[A-Z])*[a-zA-Z]?$");
        repeated_letters = Pattern.compile("([\\w|?|!|-|.])\\1{" + (min_repeated_letters - 1) + ",}");

    }

    public void loadTranslation() {
        try {
            if (!lng_file.exists()) {
                lng_file.createNewFile();
                String lang = System.getProperty("user.language").trim().toLowerCase();
                InputStream in;
                if (!traductions_in_jars.contains(lang))
                    lang = "fr";
                in = getClass().getResourceAsStream("/lang_" + lang + ".txt");
                Writer out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(lng_file), "UTF-8"));
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(new String(buf));
                }
                in.close();
                out.close();
                getLogger().info("Copied lang file (" + lang + ")");
            }
            InputStream is = new FileInputStream(lng_file);
            Properties props = new Properties();
            props.load(is);
            is.close();
            traductions.clear();
            traductions.put("anti_flood", props.getProperty("anti_flood", null));
            traductions.put("capslock", props.getProperty("capslock", null));
            traductions.put("url", props.getProperty("url", null));
            traductions.put("ip", props.getProperty("ip", null));
            traductions.put("badword", props.getProperty("badword"));
            traductions.put("config_reloaded", props.getProperty("config_reloaded", null));
            traductions.put("alternativecase", props.getProperty("alternativecase", null));
            traductions.put("repeated_letters", props.getProperty("repeated_letters", null));
            traductions.put("last_warning", props.getProperty("last_warning", null));
            traductions.put("warnings_left", props.getProperty("warnings_left", null));
            traductions.put("too_long", props.getProperty("too_long", null));

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(lng_file, true)));

            for (Map.Entry<String, String> e : traductions.entrySet()) {
                String val = e.getValue();
                if (val != null) {
                    /*
                    byte[] bytes = val.getBytes("latin1");
					val = new String(bytes, "UTF8");
					*/
                    e.setValue(ChatColor.translateAlternateColorCodes('&', val));
                } else {
                    out.println(e.getKey() + "=");
                }
            }

            out.close();
        } catch (IOException ioe) {
            getLogger().severe("IOException in loadTranslation");
            for (StackTraceElement ste : ioe.getStackTrace())
                getLogger().severe(ste.toString());
        }

    }

    public void loadWarnings() {

        if (!save_warnings_on_reload) return;
        try {

            if (!W_file.exists()) {
                W_file.createNewFile();
            }

            FileInputStream fstream = new FileInputStream(W_file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;

            while ((strLine = br.readLine()) != null) {
                String[] parts = strLine.split(":!:");
                if (parts.length == 2)
                    warnings.put(parts[0], Integer.valueOf(parts[1]));
            }
        } catch (IOException e) {
            getLogger().severe(e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent e) {
        if (ignore_nicknames) return;
        usernames.add(e.getPlayer().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onQuit(PlayerQuitEvent e) {
        if (ignore_nicknames) return;
        usernames.remove(e.getPlayer().getName());
    }

    @EventHandler()
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        if (p.hasPermission("MChat.bypass")) return;
        String message = e.getMessage().trim().replaceAll("\\s+", " ");
        for (String u : usernames)
            message = message.replaceAll(u, "");
        if (block_too_fast_talking) {
            Long time = System.currentTimeMillis();
            if (last_message_time.containsKey(p.getName())) {
                long diff = time - last_message_time.get(p.getName());
                if (diff < too_fast_talking_delay) {
                    p.sendMessage(traductions.get("anti_flood"));
                    e.setCancelled(true);
                    last_message_time.put(p.getName(), time);
                    addWarning(p, "too_fast_talking");
                    return;
                }
            }

            last_message_time.put(p.getName(), time);
        }
        if (block_double_post) {
            if (last_message.containsKey(p.getName())) {
                String last_m = last_message.get(p.getName());
                Integer count = Integer.valueOf(last_m.split("/>!</")[0]);
                String last_msg = last_m.substring(last_m.indexOf("/>!</") + 5);
                if (message.equals(last_msg)) {
                    last_message.put(p.getName(), (count + 1) + "/>!</" + message);
                    if (count >= block_repeated_times) {
                        p.sendMessage(traductions.get("anti_flood"));
                        e.setCancelled(true);
                        addWarning(p, "repeated_message");
                        return;
                    }
                } else {
                    last_message.put(p.getName(), "1/>!</" + message);
                }
            } else {
                last_message.put(p.getName(), "1/>!</" + message);
            }
        }
        if (message.isEmpty()) return;
        if (block_capslock)
            if (Pattern.matches("(.*?)[A-Z ]{4,}(.*?)", message)) {
                p.sendMessage(traductions.get("capslock"));
                addWarning(p, "capslock");
                e.setCancelled(true);
                return;
            }
        if (block_ip)
            if (ip.matcher(message).find()) {
                p.sendMessage(traductions.get("ip"));
                addWarning(p, "ip");
                e.setCancelled(true);
                return;
            }
        if (block_url) {
            Matcher M = url.matcher(message);
            while (M.find()) {
                if (containsIgnoreCase(domainWhitelist, M.group(0) + M.group(2)))
                    continue;
                p.sendMessage(traductions.get("url"));
                addWarning(p, "url");
                e.setCancelled(true);
                return;
            }
        }
        if (block_badwords) {
            String[] mots = message.replaceAll("\\W", " ").toLowerCase().split(" ");
            for (String b : mots) {
                if (badwords.contains(b)) {
                    p.sendMessage(traductions.get("badword").replace("%word%", b));
                    addWarning(p, "badwords");
                    e.setCancelled(true);
                    return;
                }
            }
        }

        if (block_badlocutions) {
            if (locutions_block_aggressive) {
                String message_no_space = message.replaceAll(" ", "").toLowerCase();
                for (String b : badlocutions) {
                    if (message_no_space.contains(b.replaceAll(" ", ""))) {
                        p.sendMessage(traductions.get("badword").replace("%word%", b));
                        addWarning(p, "locutions");
                        e.setCancelled(true);
                        return;
                    }
                }
            } else {
                for (String b : badlocutions) {
                    if (message.contains(b)) {
                        p.sendMessage(traductions.get("badword").replace("%word%", b));
                        addWarning(p, "locutions");
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
        if (max_word_length > 0) {
            String[] mots = message.replaceAll("\\W", " ").toLowerCase().split(" ");
            for (String b : mots) {
                if (b.length() > max_word_length) {
                    p.sendMessage(traductions.get("too_long").replaceAll("%word%", b));
                    addWarning(p, "too_long");
                    e.setCancelled(true);
                    return;
                }
            }
        }
        if (block_repeated_letters)
            if (repeated_letters.matcher(message).find()) {
                p.sendMessage(traductions.get("repeated_letters"));
                addWarning(p, "repeated_letters");
                e.setCancelled(true);
                return;

            }
        if (block_alternative_case && message.length() > 3) {
            Matcher Ma = alternative_case_a.matcher(message);
            Matcher MA = alternative_case_A.matcher(message);
            if (Ma.find() || MA.find()) {
                p.sendMessage(traductions.get("alternativecase"));
                addWarning(p, "alternative_case");
                e.setCancelled(true);
            }
        }

    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("MChat")) {
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("reload") && sender.hasPermission("MChat.reload")) {
                    loadbadWords();
                    loadbadLocutions();
                    reloadConfig();
                    loadConfig();
                    loadTranslation();
                    usernames.clear();
                    if (!ignore_nicknames) {
                        for (Player p : Bukkit.getOnlinePlayers())
                            usernames.add(p.getName());
                    }
                    sender.sendMessage(traductions.get("config_reloaded"));
                    return true;
                }
            }
        }
        return false;
    }
}
