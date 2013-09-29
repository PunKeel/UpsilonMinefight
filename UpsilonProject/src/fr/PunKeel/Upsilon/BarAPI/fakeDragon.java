package fr.PunKeel.Upsilon.BarAPI;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FakeDragon {

    public static final int MAX_HEALTH = 200;
    public static Integer ENTITY_ID = 6000;
    public static Map<String, FakeDragon> dragonplayers = new HashMap<String, FakeDragon>();
    public boolean visible;
    public int EntityID;
    public int x;
    public int y;
    public int z;
    public int pitch = 0;
    public int head_pitch = 0;
    public int yaw = 0;
    public byte xvel = 0;
    public byte yvel = 0;
    public byte zvel = 0;
    public float health;
    public String name;

    public FakeDragon(String name, int EntityID, Location loc) {
        this(name, EntityID, (int) Math.floor(loc.getBlockX() * 32.0D), (int) Math.floor(loc.getBlockY() * 32.0D), (int) Math.floor(loc.getBlockZ() * 32.0D));
    }

    public FakeDragon(String name, int EntityID, Location loc, float health, boolean visible) {
        this(name, EntityID, (int) Math.floor(loc.getBlockX() * 32.0D), (int) Math.floor(loc.getBlockY() * 32.0D), (int) Math.floor(loc.getBlockZ() * 32.0D), health, visible);
    }

    public FakeDragon(String name, int EntityID, int x, int y, int z) {
        this(name, EntityID, x, y, z, MAX_HEALTH, false);
    }

    public FakeDragon(String name, int EntityID, int x, int y, int z, float health, boolean visible) {
        this.name = name;
        this.EntityID = EntityID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.health = health;
        this.visible = visible;
    }

    public static void setStatus(Player player, String text, int healthpercent) {
        FakeDragon dragon = null;
        if (dragonplayers.containsKey(player.getName())) {
            dragon = dragonplayers.get(player.getName());
            if (text == null)
                text = dragon.name;

            if (healthpercent == -1)
                healthpercent = (int) (dragon.health / FakeDragon.MAX_HEALTH) * 100;
        } else if (!text.equals("")) {
            dragon = new FakeDragon(text, ENTITY_ID, player.getLocation().add(0, -200, 0));

            dragonplayers.put(player.getName(), dragon);
        }

        if (text.equals("") && dragonplayers.containsKey(player.getName())) {
            Object destroyPacket = dragon.getDestroyEntityPacket();
            General.sendPacket(player, destroyPacket);

            dragonplayers.remove(player.getName());
        } else {
            Object mobPacket = dragon.getMobPacket();
            General.sendPacket(player, mobPacket);

            dragon.health = (healthpercent / 100f) * FakeDragon.MAX_HEALTH;
            Object metaPacket = dragon.getMetadataPacket(dragon.getWatcher());
            Object teleportPacket = dragon.getTeleportPacket(player.getLocation().add(0, -200, 0));
            General.sendPacket(player, metaPacket);
            General.sendPacket(player, teleportPacket);
        }
    }

    public static void displayDragonTextBar(Plugin plugin, String text, final Player player, long length) {
        setStatus(player, text, 100);

        new BukkitRunnable() {
            @Override
            public void run() {
                setStatus(player, "", 100);
            }
        }.runTaskLater(plugin, length);
    }

    public static void displayDragonLoadingBar(final Plugin plugin, final String text, final String completeText, final Player player, final int healthAdd, final long delay, final boolean loadUp) {
        setStatus(player, "", (loadUp ? 1 : 100));

        new BukkitRunnable() {
            int health = (loadUp ? 1 : 100);

            @Override
            public void run() {
                if ((loadUp ? health < 100 : health > 1)) {
                    setStatus(player, text, health);
                    if (loadUp) {
                        health += healthAdd;
                    } else {
                        health -= healthAdd;
                    }
                } else {
                    setStatus(player, completeText, (loadUp ? 100 : 1));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            setStatus(player, "", (loadUp ? 100 : 1));
                        }
                    }.runTaskLater(plugin, 20);

                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, delay, delay);
    }

    public static void displayDragonLoadingBar(final Plugin plugin, final String text, final String completeText, final Player player, final int secondsDelay, final boolean loadUp) {
        final int healthChangePerSecond = 100 / secondsDelay / 4;

        displayDragonLoadingBar(plugin, text, completeText, player, healthChangePerSecond, 5L, loadUp);
    }

    public Object getMobPacket() {
        Class<?> mob_class = General.getCraftClass("Packet24MobSpawn");
        Object mobPacket = null;
        try {
            mobPacket = mob_class.newInstance();

            Field a = General.getField(mob_class, "a");
            a.setAccessible(true);
            a.set(mobPacket, EntityID);//Entity ID
            Field b = General.getField(mob_class, "b");
            b.setAccessible(true);
            b.set(mobPacket, EntityType.ENDER_DRAGON.getTypeId());//Mob type (ID: 64)
            Field c = General.getField(mob_class, "c");
            c.setAccessible(true);
            c.set(mobPacket, x);//X position
            Field d = General.getField(mob_class, "d");
            d.setAccessible(true);
            d.set(mobPacket, y);//Y position
            Field e = General.getField(mob_class, "e");
            e.setAccessible(true);
            e.set(mobPacket, z);//Z position
            Field f = General.getField(mob_class, "f");
            f.setAccessible(true);
            f.set(mobPacket, (byte) ((int) (pitch * 256.0F / 360.0F)));//Pitch
            Field g = General.getField(mob_class, "g");
            g.setAccessible(true);
            g.set(mobPacket, (byte) ((int) (head_pitch * 256.0F / 360.0F)));//Head Pitch
            Field h = General.getField(mob_class, "h");
            h.setAccessible(true);
            h.set(mobPacket, (byte) ((int) (yaw * 256.0F / 360.0F)));//Yaw
            Field i = General.getField(mob_class, "i");
            i.setAccessible(true);
            i.set(mobPacket, xvel);//X velocity
            Field j = General.getField(mob_class, "j");
            j.setAccessible(true);
            j.set(mobPacket, yvel);//Y velocity
            Field k = General.getField(mob_class, "k");
            k.setAccessible(true);
            k.set(mobPacket, zvel);//Z velocity

            Object watcher = getWatcher();
            Field t = General.getField(mob_class, "t");
            t.setAccessible(true);
            t.set(mobPacket, watcher);
        } catch (InstantiationException e1) {
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        }

        return mobPacket;
    }

    public Object getDestroyEntityPacket() {
        Class<?> packet_class = General.getCraftClass("Packet29DestroyEntity");
        Object packet = null;
        try {
            packet = packet_class.newInstance();

            Field a = General.getField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, new int[]{EntityID});
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return packet;
    }

    public Object getMetadataPacket(Object watcher) {
        Class<?> packet_class = General.getCraftClass("Packet40EntityMetadata");
        Object packet = null;
        try {
            packet = packet_class.newInstance();

            Field a = General.getField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, EntityID);

            Method watcher_c = General.getMethod(watcher.getClass(), "c");
            Field b = General.getField(packet_class, "b");
            b.setAccessible(true);
            b.set(packet, watcher_c.invoke(watcher));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return packet;
    }

    public Object getTeleportPacket(Location loc) {
        Class<?> packet_class = General.getCraftClass("Packet34EntityTeleport");
        Object packet = null;
        try {
            packet = packet_class.newInstance();

            Field a = General.getField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, EntityID);
            Field b = General.getField(packet_class, "b");
            b.setAccessible(true);
            b.set(packet, (int) Math.floor(loc.getX() * 32.0D));
            Field c = General.getField(packet_class, "c");
            c.setAccessible(true);
            c.set(packet, (int) Math.floor(loc.getY() * 32.0D));
            Field d = General.getField(packet_class, "d");
            d.setAccessible(true);
            d.set(packet, (int) Math.floor(loc.getZ() * 32.0D));
            Field e = General.getField(packet_class, "e");
            e.setAccessible(true);
            e.set(packet, (byte) ((int) (loc.getYaw() * 256.0F / 360.0F)));
            Field f = General.getField(packet_class, "f");
            f.setAccessible(true);
            f.set(packet, (byte) ((int) (loc.getPitch() * 256.0F / 360.0F)));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    public Object getRespawnPacket() {
        Class<?> packet_class = General.getCraftClass("Packet205ClientCommand");
        Object packet = null;
        try {
            packet = packet_class.newInstance();

            Field a = General.getField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, 1);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    public Object getWatcher() {
        Class<?> watcher_class = General.getCraftClass("DataWatcher");
        Object watcher = null;
        try {
            watcher = watcher_class.newInstance();

            Method a = General.getMethod(watcher_class, "a", new Class<?>[]{int.class, Object.class});
            a.setAccessible(true);

            a.invoke(watcher, 0, visible ? (byte) 0 : (byte) 0x20);
            a.invoke(watcher, 6, (Float) (float) health);
            a.invoke(watcher, 7, (Integer) (int) 0);
            a.invoke(watcher, 8, (Byte) (byte) 0);
            a.invoke(watcher, 10, (String) name);
            a.invoke(watcher, 11, (Byte) (byte) 1);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return watcher;
    }

}
