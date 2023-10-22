package me.linkhealth.linkhealth;

import me.linkhealth.linkhealth.commands.LinkPlayerCommand;
import me.linkhealth.linkhealth.commands.UnlinkPlayerCommand;
import me.linkhealth.linkhealth.database.HealthDatabase;
import me.linkhealth.linkhealth.listeners.HealthChangedListeners;
import me.linkhealth.linkhealth.listeners.PlayerConnectListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public final class LinkHealth extends JavaPlugin {

    private HealthDatabase healthDatabase;

    @Override
    public void onEnable() {

        LinkHealth plugin = this;

        if (!getDataFolder().exists()){
            getDataFolder().mkdir();
        }

        try{
            healthDatabase = new HealthDatabase(getDataFolder().getAbsolutePath() + "/linkhealth.db");
        }catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to connect to the database");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        Bukkit.getPluginManager().registerEvents(new PlayerConnectListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HealthChangedListeners(this), this);
        getCommand("linkhealth").setExecutor(new LinkPlayerCommand(this));
        getCommand("unlinkhealth").setExecutor(new UnlinkPlayerCommand(this));
        // SET DESCRIPTION TO COMMANDS
    }

    @Override
    public void onDisable() {
        try{
            healthDatabase.closeConnection();
        }catch (SQLException ex){
            ex.printStackTrace();
        }
    }

    public HealthDatabase getHealthDatabase() {
        return healthDatabase;
    }
}
