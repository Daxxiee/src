package me.matt.gtaestate.commands;

import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.matt.gtaestate.Home;
import me.matt.gtaestate.HomesManager;
import me.matt.gtaestate.HomesPlugin;

public class HomeAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This Command is only for Players!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0] == null) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("create")) {
            if (args.length == 2) {
                try {
                    Selection selection = HomesPlugin.worldEdit.getSelection(player);
                    if (selection.getWorld() != player.getWorld()) {
                        player.sendMessage(ChatColor.RED + "Please select an area, then stand at the house TP location.");
                        return true;
                    }
                    HomesManager.homes.put(args[1], new Home(true, player.getLocation(),
                            selection.getMinimumPoint().getBlockX(), selection.getMinimumPoint().getBlockY(), selection.getMinimumPoint().getBlockZ(), selection.getMaximumPoint().getBlockX(), selection.getMaximumPoint().getBlockY(), selection.getMaximumPoint().getBlockZ()));
                    player.sendMessage(ChatColor.GREEN + "Home " + args[1] + " created!");
                    HomesManager.save();
                } catch (Exception e) {
                    player.sendMessage(ChatColor.RED + "Please select an area, then stand at the house TP location.");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/home create <name>");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("delete")) {
            if (args.length == 2) {
                try {
                    if (HomesManager.homes.remove(args[1]) == null) {
                        player.sendMessage(ChatColor.RED + "Home named " + args[1] + " does not exist! (CASE-SENSITIVE)");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Home" + args[1] + "removed!");
                        HomesManager.save();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                sender.sendMessage(ChatColor.RED + "/home delete <name>");
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "-------------" + ChatColor.RED + " Homes Admin " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-------------\n" +
                ChatColor.RED + "/HomeAdmin create <name>" + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Create a home. (Select with WorldEdit WAND then stand on TP location.)\n" +
                ChatColor.RED + "/HomeAdmin delete <name>" + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Delete a home.\n" +
                ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "-------------" + ChatColor.RED + " Homes Admin " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-------------");
    }

}
