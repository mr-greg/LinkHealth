package me.linkhealth.linkhealth.listeners;

import me.linkhealth.linkhealth.LinkHealth;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;


public class PlayerConnectListener implements Listener {

    private final LinkHealth linkHealthPlugin;

    public PlayerConnectListener(LinkHealth linkHealthPlugin) {
        this.linkHealthPlugin = linkHealthPlugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) throws SQLException {
        Player p = event.getPlayer();
        if (!p.hasPlayedBefore()) {
            try{
                this.linkHealthPlugin.getHealthDatabase().addPlayer(p);
                System.out.println("player connect first time, entered the try");
                return;
            }catch (SQLException ex){
                ex.printStackTrace();
                p.sendMessage(ChatColor.RED + "Une erreur est survenue lors de l'inscription en BDD, merci de contacter Zaack (ERROR CODE #01)");
            }
        }
        p.setHealth(linkHealthPlugin.getHealthDatabase().getHealth(p));
    }
}
