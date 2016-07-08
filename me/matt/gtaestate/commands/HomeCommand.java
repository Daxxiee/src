package me.matt.gtaestate.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import me.matt.gtaestate.Home;
import me.matt.gtaestate.HomesManager;
import me.matt.gtaestate.HomesPlugin;
import me.matt.gtaestate.Utils;

import java.util.UUID;
import java.util.regex.Pattern;

public class HomeCommand implements CommandExecutor {

    private static final Pattern vaildUsernamePattern = Pattern.compile("^[a-zA-Z0-9_]*$");

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

        if (args[0].equalsIgnoreCase("info")) {
            if (args.length == 2) {
                if (args[1].length() > 0 && args[1].length() <= 16
                        && vaildUsernamePattern.matcher(args[1]).matches()) {
                    Home home = HomesManager.getHome(args[1]);
                    if (home == null) {
                        sender.sendMessage(ChatColor.RED + "Can't find that player's house");
                    } else {
                        sendHomeInfo(sender, home);
                    }
                }
            } else {
                Home home = HomesManager.getHome(player);
                if (home == null) {
                    sender.sendMessage(ChatColor.RED + "You do not have a home!");
                } else {
                    sendHomeInfo(sender, home);
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("menu")) {
            sender.sendMessage(ChatColor.RED + "Coming soon!"); //TODO
            return true;
        }

        if (args[0].equalsIgnoreCase("find")) {
            HomesManager.newHome(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("renew")) {
            Home home = HomesManager.getHome(player);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home!");
            } else {
                if (HomesPlugin.economy.has(player, HomesPlugin.getInstance().getConfig().getDouble("renew_price"))) {
                    HomesPlugin.economy.withdrawPlayer(player, HomesPlugin.getInstance().getConfig().getDouble("renew_price"));
                    home.newExpiry();
                    player.sendMessage(ChatColor.GREEN + "Your home has been renewed!");
                } else {
                    player.sendMessage(ChatColor.RED + "You need to have $" + HomesPlugin.getInstance().getConfig().getDouble("renew_price") + " to renew your home!");
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("tp")) {
            if (args.length == 1) {
                Home home = HomesManager.getHome(player);
                if (home == null) {
                    player.sendMessage(ChatColor.RED + "You do not have a home!");
                } else {
                    player.teleport(home.location);
                    player.sendMessage(ChatColor.GREEN + "You have been teleported to your home!");
                }
            } else if (args.length == 2) {
                if (args[1].length() > 0 && args[1].length() <= 16
                        && vaildUsernamePattern.matcher(args[1]).matches()) {
                    Home home = HomesManager.getHome(args[1]);
                    if (home == null) {
                        player.sendMessage(ChatColor.RED + args[1] + " does not own a home!");
                    } else {
                        if (home.allowed.contains(player.getUniqueId())) {
                            player.teleport(home.location);
                            player.sendMessage(ChatColor.GREEN + "You have been teleported to " + home.lastKnownOwnerName + "'s home!");
                        } else {
                            player.sendMessage(ChatColor.RED + "You are not allowed to teleport to this home! Ask the home owner to add you.");
                        }
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Invalid username!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "/" + label + " tp [player]");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("add")) {
            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "/" + label + " add <name>");
                return true;
            }

            Home home = HomesManager.getHome(player);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home!");
            } else {
                if (args[1].length() > 0 && args[1].length() <= 16
                        && vaildUsernamePattern.matcher(args[1]).matches()) {
                    OfflinePlayer added = Bukkit.getOfflinePlayer(args[1]);
                    if (added != null && added.hasPlayedBefore()) {
                        if (added == player) {
                            player.sendMessage(ChatColor.RED + "You can't add yourself!");
                            return true;
                        }
                        home.allowed.add(added.getUniqueId());
                        player.sendMessage(ChatColor.GREEN + added.getName() + " has been added to your home!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid username!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid username!");
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (args.length != 2) {
                player.sendMessage(ChatColor.RED + "/" + label + " add <name>");
                return true;
            }

            Home home = HomesManager.getHome(player);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home!");
            } else {
                if (args[1].length() > 0 && args[1].length() <= 16
                        && vaildUsernamePattern.matcher(args[1]).matches()) {
                    OfflinePlayer removed = Bukkit.getOfflinePlayer(args[1]);
                    if (removed != null && removed.hasPlayedBefore()) {
                        if (removed == player) {
                            player.sendMessage(ChatColor.RED + "You can't remove yourself!");
                            return true;
                        }
                        if (home.allowed.remove(removed.getUniqueId())) {
                            player.sendMessage(ChatColor.GREEN + removed.getName() + " has been removed from your home!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Player was not member of your home!");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "Invalid username!");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Invalid username!");
                }
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("lock")) {
            Home home = HomesManager.getHome(player);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home!");
            } else {
                home.locked = true;
                player.sendMessage(ChatColor.GREEN + "Your home has been locked!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("unlock")) {
            Home home = HomesManager.getHome(player);
            if (home == null) {
                player.sendMessage(ChatColor.RED + "You do not have a home!");
            } else {
                home.locked = false;
                player.sendMessage(ChatColor.GREEN + "Your home has been unlocked!");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("debug")) {
            player.sendMessage(ChatColor.RED + "Debugging Estates");
            player.sendMessage("Estate Ownerships...");
            player.sendMessage("" + ChatColor.GREEN + "Clean!");
            player.sendMessage("Estate Listings...");
            player.sendMessage("" + ChatColor.GREEN + "Clean!");
            player.sendMessage("Estate Balances...");
            player.sendMessage("" + ChatColor.GREEN + "Clean!");
            player.sendMessage("Estate Friends...");
            player.sendMessage("" + ChatColor.GREEN + "Clean!");
            player.sendMessage("Estate Lockings...");
            player.sendMessage("" + ChatColor.GREEN + "Clean!");
            player.sendMessage("Debug Finished!");
            player.sendMessage("Found " + ChatColor.GREEN + "0" + ChatColor.WHITE + " Estate Errors!");
            }
            return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "-------------" + ChatColor.RED + " Estates " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-------------\n" +
                ChatColor.RED + "/Estate Info " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Lists home information.\n" +
                ChatColor.RED + "/Estate Find " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Finds you a home.\n" +
                ChatColor.RED + "/Estate Renew " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Renews your home.\n" +
                ChatColor.RED + "/Estate Tp <name> " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Teleports to a home.\n" +
                ChatColor.RED + "/Estate Add <name> " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Adds a user to your home.\n" +
                ChatColor.RED + "/Estate Remove <name> " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Removes a user from your home.\n" +
                ChatColor.RED + "/Estate Lock " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Locks your home from others entering.\n" +
                ChatColor.RED + "/Estate Unlock " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Unlocks your home and allows others to enter.\n" +
                ChatColor.RED + "/EstateAdmin " + ChatColor.DARK_GRAY + "»" + ChatColor.GRAY + " Admin commands.\n" +
                ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-------------" + ChatColor.RED + " Estates " + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH + "-------------");
    }

    public static void sendHomeInfo(CommandSender sender, Home home) {
        sender.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "--------- " + ChatColor.RED + ChatColor.BOLD + "Home Info " + "(" + home.lastKnownOwnerName + ")" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + " ---------");

        sender.sendMessage(ChatColor.RED + "Locked: " + ChatColor.GRAY + Utils.toString(home.locked));

        if (home.allowed.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (UUID uuid : home.allowed) {
                sb.append(Bukkit.getOfflinePlayer(uuid).getName() + ", ");
            }
            sender.sendMessage(ChatColor.RED + "Members (" + (home.allowed.size()) + "): " + ChatColor.GRAY + sb.subSequence(0, sb.length() - 2));
        }

        sender.sendMessage(ChatColor.RED + "Expires In: " + ChatColor.GRAY + Utils.getTimeLeft(home.expiry));

        sender.sendMessage(ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + "--------- " + ChatColor.RED + ChatColor.BOLD + "Home Info " + "(" + home.lastKnownOwnerName + ")" + ChatColor.DARK_GRAY + ChatColor.STRIKETHROUGH.toString() + " ---------");
    }

}