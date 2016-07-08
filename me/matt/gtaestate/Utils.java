package me.matt.gtaestate;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class Utils {

    public static String toString(boolean bool) {
        return bool ? "true" : "false";
    }

    public static String getTimeLeft(long milli) {
        return toString(milli - System.currentTimeMillis());
    }

    public static String toString(long milliseconds) {
        long days = TimeUnit.MILLISECONDS.toDays(milliseconds);
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);

        List<String> strings = new ArrayList<>();

        if (days > 0)
            strings.add(String.valueOf(days) + " days");

        if (hours > 0)
            strings.add(String.valueOf(hours) + " hours");

        if (minutes > 0)
            strings.add(String.valueOf(minutes) + " minutes");

        if (seconds > 0) {
            strings.add(String.valueOf(seconds) + " seconds");
        }

        String[] array = strings.toArray(new String[strings.size()]);

        if (array.length == 0) {
            return "";
        }

        if (array.length == 1) {
            return array[0];
        }

        if (array.length == 2) {
            return array[0] + " and " + array[1];
        }

        if (array.length == 3) {
            return array[0] + ", " + array[1] + " and " + array[2];
        }

        return array[0] + ", " + array[1] + ", " + array[2] + " and " + array[3];
    }

    public static String signTime(Home home) {
        long timeUntilExpiry = home.expiry - System.currentTimeMillis();
        if (timeUntilExpiry < 1000L) return "00:00:00:00";
        long days = TimeUnit.MILLISECONDS.toDays(timeUntilExpiry);
        long hours = TimeUnit.MILLISECONDS.toHours(timeUntilExpiry) - TimeUnit.DAYS.toHours(days);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeUntilExpiry) - TimeUnit.HOURS.toMinutes(hours) - TimeUnit.DAYS.toMinutes(days);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeUntilExpiry) - TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.DAYS.toSeconds(days);
        return days + ":" + hours + ":" + minutes + ":" + seconds;
    }

    public static class UUIDSerializer implements ScalarSerializer<UUID> {

        @Override
        public String write(UUID uuid) throws YamlException {
            return uuid.toString();
        }

        @Override
        public UUID read(String s) throws YamlException {
            return UUID.fromString(s);
        }

    }

    public static class LocationSerializer implements ScalarSerializer<Location> {

        private JsonParser parser = new JsonParser();

        @Override
        public String write(Location location) throws YamlException {
            JsonObject jo = new JsonObject();
            jo.addProperty("world", location.getWorld().getName());
            jo.addProperty("x", location.getX());
            jo.addProperty("y", location.getY());
            jo.addProperty("z", location.getZ());
            jo.addProperty("yaw", location.getYaw());
            jo.addProperty("pitch", location.getPitch());
            return jo.toString();
        }

        @Override
        public Location read(String s) throws YamlException {
            JsonObject jo = parser.parse(s).getAsJsonObject();
            return new Location(Bukkit.getWorld(jo.get("world").getAsString()), jo.get("x").getAsInt(), jo.get("y").getAsInt(), jo.get("z").getAsInt(), jo.get("yaw").getAsFloat(), jo.get("pitch").getAsFloat());
        }

    }

    public static class WorldSerializer implements ScalarSerializer<World> {

        @Override
        public String write(World world) throws YamlException {
            return world.getName();
        }

        @Override
        public World read(String s) throws YamlException {
            return Bukkit.getWorld(s);
        }

    }

}
