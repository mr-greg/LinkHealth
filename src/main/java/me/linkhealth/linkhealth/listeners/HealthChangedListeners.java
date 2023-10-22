package me.linkhealth.linkhealth.listeners;

import me.linkhealth.linkhealth.LinkHealth;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.sql.SQLException;
import java.util.UUID;

public class HealthChangedListeners implements Listener {
    private final LinkHealth linkHealthPlugin;

    public HealthChangedListeners(LinkHealth linkHealthPlugin) {
        this.linkHealthPlugin = linkHealthPlugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) throws SQLException {
        if (!(e.getEntity() instanceof Player)) return;

        // Get the player
        Player p = ((Player) e.getEntity()).getPlayer();
        // Check if player is linked
        if (!linkHealthPlugin.getHealthDatabase().isPlayerLinked(p)) return;

        // Gets the linked player
        String partnerUUID = linkHealthPlugin.getHealthDatabase().getLinkedPlayer(p);
        Player partner = Bukkit.getPlayer(UUID.fromString(partnerUUID));


        double playerHealth = ((Player) e.getEntity()).getHealth();
        double newHealth = playerHealth - e.getFinalDamage();

        if (newHealth < 0) newHealth = 0;
        linkHealthPlugin.getHealthDatabase().updateHealthDatabase(p, newHealth, true);


        if (partner != null && partner.isOnline()) {
            partner.sendHurtAnimation(0);
            partner.playSound(partner, Sound.ENTITY_PLAYER_HURT, 10, 10);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) throws SQLException {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = ((Player) e.getEntity()).getPlayer();
        if (!linkHealthPlugin.getHealthDatabase().isPlayerLinked(p)) return;

        double playerHealth = ((Player) e.getEntity()).getHealth();
        double newHealth  = Math.round(playerHealth + e.getAmount());

        if (newHealth > 20) newHealth = 20;

        linkHealthPlugin.getHealthDatabase().updateHealthDatabase(p, newHealth, false);
    }
}
