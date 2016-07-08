package me.matt.gtaestate;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import me.matt.gtaestate.commands.HomeAdminCommand;
import me.matt.gtaestate.commands.HomeCommand;
import me.matt.gtaestate.listeners.PlotInteractionListener;
import me.matt.gtaestate.listeners.SignListener;

public class HomesPlugin extends JavaPlugin implements Listener {

    private static HomesPlugin instance;

    //External APIs
    public static Economy economy;
    public static WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        economy = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider(); //Has to be here, Exception when initialized directly.

        HomesManager.init();

        getCommand("estate").setExecutor(new HomeCommand());
        getCommand("estateadmin").setExecutor(new HomeAdminCommand());

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new PlotInteractionListener(), this);
        getServer().getPluginManager().registerEvents(new SignListener(), this);

        super.onEnable();
    }

    @Override
    public void onDisable() {
        HomesManager.shutdown();
        instance = null;
        economy = null;
        worldEdit = null;
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Home home = HomesManager.getHome(event.getPlayer());
        if (home != null) home.updateOwner(event.getPlayer()); //Updates name, just in case it has changed.
    }

    public static HomesPlugin getInstance() {
        return instance;
    }

}
