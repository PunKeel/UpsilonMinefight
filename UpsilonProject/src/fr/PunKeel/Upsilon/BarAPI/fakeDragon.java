package fr.PunKeel.Upsilon.BarAPI;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FakeDragon {

    private static final int MAX_HEALTH = 200;
    private static final Map<String, FakeDragon> dragonplayers = new HashMap<>();
    private final boolean visible;
    private final int EntityID;
    private final int x;
    private final int y;
    private final int z;
    private float health;
    private String name;

    private FakeDragon(String name, int EntityID, Location loc) {
        this(name, EntityID, (int) Math.floor(loc.getBlockX() * 32.0D), (int) Math.floor(loc.getBlockY() * 32.0D), (int) Math.floor(loc.getBlockZ() * 32.0D));
    }

    private FakeDragon(String name, int EntityID, int x, int y, int z) {
        this.name = name;
        this.EntityID = EntityID;
        this.x = x;
        this.y = y;
        this.z = z;
        this.health = (float) FakeDragon.MAX_HEALTH;
        this.visible = false;
    }

    public static void setStatus(Player player, String text, int healthpercent) {
        FakeDragon dragon;
        boolean new_entity = false, update_entity = false, remove_entity = false;
        if (hasBar(player)) {
            dragon = dragonplayers.get(player.getName());
            if (text != null) {
                if (!dragon.name.equals(text)) {
                    dragon.name = text;
                    update_entity = true;
                }
            } else {
                remove_entity = true;
            }
            if (healthpercent != -1) {
                if (dragon.health != (dragon.health / FakeDragon.MAX_HEALTH) * 100) {
                    update_entity = true;
                    dragon.health = (dragon.health / FakeDragon.MAX_HEALTH) * 100;
                }
            }

        } else {
            dragon = new FakeDragon(text, 6000, player.getLocation().add(0, -200, 0));
            if (healthpercent != -1) {

                dragon.health = (healthpercent / 100) * FakeDragon.MAX_HEALTH;
            } else {
                dragon.health = 0;
            }
            new_entity = true;
            dragonplayers.put(player.getName(), dragon);
        }


        if (remove_entity && !update_entity) {
            Object destroyPacket = dragon.getDestroyEntityPacket();
            General.sendPacket(player, destroyPacket);

            dragonplayers.remove(player.getName());
            return;
        }
        if (new_entity) {
            Object mobPacket = dragon.getMobPacket();
            General.sendPacket(player, mobPacket);
        } else {
            Object metaPacket = dragon.getMetadataPacket(dragon.getWatcher());
            Object teleportPacket = dragon.getTeleportPacket(player.getLocation().subtract(0, 200, 0));
            General.sendPacket(player, metaPacket);
            General.sendPacket(player, teleportPacket);
        }
    }

    public static boolean hasBar(Player p) {
        return dragonplayers.containsKey(p.getName());
    }

    Object getMobPacket() {
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
            int pitch = 0;
            f.set(mobPacket, (byte) ((int) (pitch * 256.0F / 360.0F)));//Pitch
            Field g = General.getField(mob_class, "g");
            g.setAccessible(true);
            int head_pitch = 0;
            g.set(mobPacket, (byte) ((int) (head_pitch * 256.0F / 360.0F)));//Head Pitch
            Field h = General.getField(mob_class, "h");
            h.setAccessible(true);
            int yaw = 0;
            h.set(mobPacket, (byte) ((int) (yaw * 256.0F / 360.0F)));//Yaw
            Field i = General.getField(mob_class, "i");
            i.setAccessible(true);
            byte xvel = 0;
            i.set(mobPacket, xvel);//X velocity
            Field j = General.getField(mob_class, "j");
            j.setAccessible(true);
            byte yvel = 0;
            j.set(mobPacket, yvel);//Y velocity
            Field k = General.getField(mob_class, "k");
            k.setAccessible(true);
            byte zvel = 0;
            k.set(mobPacket, zvel);//Z velocity

            Object watcher = getWatcher();
            Field t = General.getField(mob_class, "t");
            t.setAccessible(true);
            t.set(mobPacket, watcher);
        } catch (InstantiationException | IllegalAccessException e1) {
            e1.printStackTrace();
        }

        return mobPacket;
    }

    Object getDestroyEntityPacket() {
        Class<?> packet_class = General.getCraftClass("Packet29DestroyEntity");
        Object packet = null;
        try {
            packet = packet_class.newInstance();

            Field a = General.getField(packet_class, "a");
            a.setAccessible(true);
            a.set(packet, new int[]{EntityID});
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return packet;
    }

    Object getMetadataPacket(Object watcher) {
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
        } catch (InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return packet;
    }

    Object getTeleportPacket(Location loc) {
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
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return packet;
    }

    Object getWatcher() {
        Class<?> watcher_class = General.getCraftClass("DataWatcher");
        Object watcher = null;
        try {
            watcher = watcher_class.newInstance();

            Method a = General.getMethod(watcher_class, "a", new Class<?>[]{int.class, Object.class});
            a.setAccessible(true);

            a.invoke(watcher, 0, visible ? (byte) 0 : (byte) 0x20);
            a.invoke(watcher, 6, health);
            a.invoke(watcher, 7, 0);
            a.invoke(watcher, 8, (byte) 0);
            a.invoke(watcher, 10, name);
            a.invoke(watcher, 11, (byte) 1);
        } catch (InstantiationException | InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return watcher;
    }

}
