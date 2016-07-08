package me.matt.gtaestate.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import me.matt.gtaestate.Home;
import me.matt.gtaestate.HomesManager;
import me.matt.gtaestate.HomesPlugin;
import me.matt.gtaestate.commands.HomeCommand;

public class SignListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (event.getPlayer().hasPermission("homes.admin")) {
            if (event.getLine(0) != null && event.getLine(0).equalsIgnoreCase("home")
                    && event.getLine(1) != null && HomesManager.homes.containsKey(event.getLine(1))) {
                Home home = HomesManager.homes.get(event.getLine(1));
                home.signs.add(event.getBlock().getLocation());

                //Has to be done like this to update sign first time.
                String[] lines = home.updateSigns();
                event.setLine(0, lines[0]);
                event.setLine(1, lines[1]);
                event.setLine(2, lines[2]);
                event.setLine(3, lines[3]);

                event.getPlayer().sendMessage(ChatColor.GREEN + "Sign created!");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !(event.getClickedBlock().getState() instanceof Sign))
            return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        Home home = getSignHome(sign);
        if (home == null) return;
        if (home.isOccupied()) {
            HomeCommand.sendHomeInfo(event.getPlayer(), home);
        } else {
            if (HomesManager.getHome(event.getPlayer()) != null) {
                event.getPlayer().sendMessage(ChatColor.RED + "You already have a home! /home tp");
                return;
            }
            if (HomesPlugin.economy.has(event.getPlayer(), HomesPlugin.getInstance().getConfig().getDouble("initial_price"))) {
                HomesPlugin.economy.withdrawPlayer(event.getPlayer(), HomesPlugin.getInstance().getConfig().getDouble("initial_price"));
                home.updateOwner(event.getPlayer());
                event.getPlayer().teleport(home.location);
                event.getPlayer().sendMessage(ChatColor.GREEN + "You now own this home! " + ChatColor.RED + "(-$" + HomesPlugin.getInstance().getConfig().getDouble("initial_price") + ")");
            } else {
                event.getPlayer().sendMessage(ChatColor.RED + "You need to have $" + HomesPlugin.getInstance().getConfig().getDouble("initial_price") + " to find a home!");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) return;

        Sign sign = (Sign) event.getBlock().getState();
        Home home = getSignHome(sign);
        if (home == null) return;

        event.setCancelled(true);

        if (!event.getPlayer().hasPermission("homes.admin")) return;
        if (event.getPlayer().isSneaking()) {
            sign.getBlock().setType(Material.AIR);
            home.signs.remove(sign.getLocation());
            event.getPlayer().sendMessage(ChatColor.GREEN + "Sign removed!");
        } else {
            event.getPlayer().sendMessage(ChatColor.RED + "You must be sneaking to remove a Home sign!");
        }
    }
    
    private static Home getSignHome(Sign sign) {
        for (Home home : HomesManager.homes.values())
            for (Location l : home.signs) if (l.equals(sign.getLocation())) return home;
        return null;
    }

}
