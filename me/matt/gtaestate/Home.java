package me.matt.gtaestate;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Home {

    public UUID owner;
    public String lastKnownOwnerName;
    public boolean locked = true;
    public Location location;

    public int minX;
    public int minY;
    public int minZ;

    public int maxX;
    public int maxY;
    public int maxZ;

    public long firstPurchased;
    public long expiry = Long.MAX_VALUE;
    public Set<UUID> allowed = new HashSet<>();

    public Set<Location> signs = new HashSet<>();

    public Home(boolean locked, Location location, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.locked = locked;
        this.location = location;

        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    @Deprecated
    public Home() {
    }

    public boolean isIn(Location location) {
        return location.getWorld() == this.location.getWorld()
                && location.getX() >= minX && location.getX() <= maxX
                && location.getY() >= minY && location.getY() <= maxY
                && location.getZ() >= minZ && location.getZ() <= maxZ;
    }

    public boolean isIn(Block block) {
        return isIn(block.getLocation());
    }

    public boolean isIn(Entity entity) {
        return isIn(entity.getLocation());
    }

    public int clearLand() {
        int blocks = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block b = location.getWorld().getBlockAt(x, y, z);
                    if (b.getType() != null || b.getType() != Material.AIR) {
                        b.setType(Material.AIR);
                        blocks++;
                    }
                }
            }
        }
        return blocks;
    }

    public void clear() {
        this.owner = null;
        this.lastKnownOwnerName = null;
        this.locked = true;
        this.firstPurchased = 0;
        this.expiry = 0;
        this.allowed.clear();
        this.clearLand();
        updateSigns();
    }

    public void updateOwner(Player player) {
        if (owner == null || !owner.equals(player.getUniqueId())) {
            this.clear();
            this.firstPurchased = System.currentTimeMillis();
            newExpiry();
        }
        this.owner = player.getUniqueId();
        this.lastKnownOwnerName = player.getName();
        updateSigns();
    }

    public boolean isOccupied() {
        return owner != null;
    }

    public void allow(Player... allowed) {
        if (allowed != null && allowed.length > 1) {
            for (Player player : allowed) {
                if (player != null) {
                    this.allowed.add(player.getUniqueId());
                }
            }
        }
    }

    public boolean allowed(Player player) {
        return player.getUniqueId().equals(owner) || (!locked && allowed.contains(player.getUniqueId()));
    }

    public boolean hasExpired() {
        return System.currentTimeMillis() > expiry;
    }

    public long newExpiry() {
        return expiry = System.currentTimeMillis() + (HomesPlugin.getInstance().getConfig().getInt("hours_until_house_expire") * 3600000L); //60(m) * 60(s) * 1000(ms) = 3600000 (ms)
    }

    public String[] updateSigns() {
        String[] lines = new String[4];
        if (this.isOccupied()) {
            lines[0] = ChatColor.RED + "[Estate]";
            lines[1] = occupiedLine(HomesPlugin.getInstance().getConfig().getString("signs.occupied.Line2"));
            lines[2] = occupiedLine(HomesPlugin.getInstance().getConfig().getString("signs.occupied.Line3"));
            lines[3] = occupiedLine(HomesPlugin.getInstance().getConfig().getString("signs.occupied.Line4"));
        } else {
            lines[0] = ChatColor.GREEN + "[Estate]";
            lines[1] = availableLine(HomesPlugin.getInstance().getConfig().getString("signs.available.Line2"));
            lines[2] = availableLine(HomesPlugin.getInstance().getConfig().getString("signs.available.Line3"));
            lines[3] = availableLine(HomesPlugin.getInstance().getConfig().getString("signs.available.Line4"));
        }

        Set<Location> remove = new HashSet<>();
        for (Location location : signs) {
            if (location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) {
                Block b = location.getBlock();
                b.getChunk().isLoaded();
                if (b.getState() instanceof Sign) {
                    Sign sign = (Sign) b.getState();
                    sign.setLine(0, lines[0]);
                    sign.setLine(1, lines[1]);
                    sign.setLine(2, lines[2]);
                    sign.setLine(3, lines[3]);
                    sign.update();
                } else {
                    remove.add(location);
                }
            }
        }
        for (Location r : remove) {
            signs.remove(r);
        }

        return lines;
    }

    private String availableLine(String s) {
        return ChatColor.translateAlternateColorCodes('&', s
                .replace("{OWNER}", "Unowned")
                .replace("{PRICE}", String.valueOf(HomesPlugin.getInstance().getConfig().getDouble("initial_price")))
                .replace("{TIME}", HomesPlugin.getInstance().getConfig().getInt("hours_until_house_expire") + " hours"));
    }

    private String occupiedLine(String s) {
        return ChatColor.translateAlternateColorCodes('&', s
                .replace("{OWNER}", lastKnownOwnerName)
                .replace("{PRICE}", String.valueOf(HomesPlugin.getInstance().getConfig().getDouble("renew_price")))
                .replace("{TIME}", Utils.signTime(this)));
    }

}
