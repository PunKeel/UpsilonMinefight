package fr.PunKeel.Upsilon;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonObject;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonSerializationContext;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class SLocation {

    private double x;
    private double y;
    private double z;
    private double yaw;
    private String world;

    public SLocation(Location location) {
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        world = location.getWorld().getName();
        yaw = location.getYaw();
    }

    public SLocation(String world, int x, int y, int z, int yaw) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
    }

    public String getWorld() {
        return world;
    }

    public Location toLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z, (long) yaw, (long) 0x1);
    }

    public double getZ() {
        return z;
    }

    public double getY() {
        return y;
    }

    public double getX() {
        return x;
    }

    public double getYaw() {
        return yaw;
    }
}
class SLocationSerialiser implements JsonSerializer<SLocation> {

    public SLocationSerialiser() {
        super();
    }

    @Override
    public JsonElement serialize(final SLocation value, final Type type,
                                 final JsonSerializationContext context) {
        final JsonObject jsonObj = new JsonObject();
        jsonObj.add("x", context.serialize(value.getX()));
        jsonObj.add("y", context.serialize(value.getY()));
        jsonObj.add("z", context.serialize(value.getZ()));
        jsonObj.add("yaw", context.serialize(value.getYaw()));
        jsonObj.add("world", context.serialize(value.getWorld()));

        return jsonObj;
    }
}