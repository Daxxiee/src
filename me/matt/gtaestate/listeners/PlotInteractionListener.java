package me.matt.gtaestate.listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import me.matt.gtaestate.Home;
import me.matt.gtaestate.HomesManager;

public class PlotInteractionListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_AIR) {
            for (Home home : HomesManager.homes.values()) {
                if (home.allowed(event.getPlayer())) continue;
                if (home.isIn(event.getPlayer())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You can not interact on home territory!");
                    event.setCancelled(true);
                    return;
                }
                if (event.getClickedBlock() != null && home.isIn(event.getClickedBlock())) {
                    event.getPlayer().sendMessage(ChatColor.RED + "You can not interact on home territory!");
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(cancel(event.getPlayer(), event.getBlock()));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(cancel(event.getPlayer(), event.getBlock()));
    }

    private static boolean cancel(Player player, Block b) { //If Block is in Home region, cancel.
        for (Home home : HomesManager.homes.values()) {
            if (home.isIn(b) && !(home.isOccupied() && (home.owner.equals(player.getUniqueId()) || home.allowed(player)))) {
                player.sendMessage(ChatColor.RED + "You can not build on home territory!");
                return true;
            }
        }
        return false;
    }

}
