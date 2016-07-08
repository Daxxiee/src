package me.matt.gtaestate;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HomesManager {

    private static File userHomesDataFile;

    public static final Map<String, Home> homes = new ConcurrentHashMap<>();

    public static void init() {
        try {
            userHomesDataFile = new File(HomesPlugin.getInstance().getDataFolder(), "homes");

            if (userHomesDataFile.exists()) {
                YamlReader reader = new YamlReader(new FileReader(userHomesDataFile));
                reader.getConfig().writeConfig.setAutoAnchor(false);
                reader.getConfig().setScalarSerializer(UUID.class, new Utils.UUIDSerializer());
                reader.getConfig().setScalarSerializer(Location.class, new Utils.LocationSerializer());
                reader.getConfig().setScalarSerializer(World.class, new Utils.WorldSerializer());
                homes.putAll(reader.read(Map.class));

                for (Home home : homes.values()) home.updateSigns();
            }

            Bukkit.getScheduler().scheduleAsyncRepeatingTask(HomesPlugin.getInstance(), () -> {
                try {
                    save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 300L, 300L);
            Bukkit.getScheduler().scheduleAsyncRepeatingTask(HomesPlugin.getInstance(), () -> {
                for (Home home : homes.values()) {
                    if (home.hasExpired()) {
                        Bukkit.getScheduler().runTask(HomesPlugin.getInstance(), () -> {
                            if (home.hasExpired()) //Just in case renewed during that small amount of time. (Unlikely)
                                home.clear();
                        });
                    }
                }
            }, 20L, 20L);
            Bukkit.getScheduler().scheduleSyncRepeatingTask(HomesPlugin.getInstance(), () -> {
                for (Home home : homes.values()) home.updateSigns();
            }, 20L, 20L);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shutdown() {
        try {
            save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() throws IOException {
        YamlWriter writer = new YamlWriter(new FileWriter(userHomesDataFile));
        writer.getConfig().writeConfig.setAutoAnchor(false);
        writer.getConfig().setScalarSerializer(UUID.class, new Utils.UUIDSerializer());
        writer.getConfig().setScalarSerializer(Location.class, new Utils.LocationSerializer());
        writer.getConfig().setScalarSerializer(World.class, new Utils.WorldSerializer());
        writer.write(homes);
        writer.close();
    }

    public static void newHome(Player player) {
        if (getHome(player) != null) {
            player.sendMessage(ChatColor.RED + "You already have a home! /home tp");
            return;
        }
        if (HomesPlugin.economy.has(player, HomesPlugin.getInstance().getConfig().getDouble("initial_price"))) {
            Home home = emptyHome();
            if (home == null) {
                player.sendMessage(ChatColor.RED + "There are no new homes available right now! Please try again later!");
                return;
            }
            HomesPlugin.economy.withdrawPlayer(player, HomesPlugin.getInstance().getConfig().getDouble("initial_price"));
            home.updateOwner(player);
            player.teleport(home.location);
            player.sendMessage(ChatColor.GREEN + "You now own this home! " + ChatColor.RED + "(-$" + HomesPlugin.getInstance().getConfig().getDouble("initial_price") + ")");
        } else {
            player.sendMessage(ChatColor.RED + "You need to have $" + HomesPlugin.getInstance().getConfig().getDouble("initial_price") + " to find a home!");
        }
    }

    private static Home emptyHome() {
        for (Home home : homes.values()) {
            if (!home.isOccupied()) return home;
        }
        return null;
    }

    public static Home getHome(Player player) {
        for (Home home : homes.values()) {
            if (home.owner != null && home.owner.equals(player.getUniqueId())) return home;
        }
        return null;
    }

    public static Home getHome(String playerName) {
        for (Home home : homes.values()) {
            if (home.lastKnownOwnerName != null && home.lastKnownOwnerName.equalsIgnoreCase(playerName)) return home;
        }
        return null;
    }

}
