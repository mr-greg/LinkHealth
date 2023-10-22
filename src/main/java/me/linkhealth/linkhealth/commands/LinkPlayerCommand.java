package me.linkhealth.linkhealth.commands;

import me.linkhealth.linkhealth.LinkHealth;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class LinkPlayerCommand implements CommandExecutor {
    private final LinkHealth linkHealthPlugin;

    public LinkPlayerCommand(LinkHealth linkHealthPlugin) {
        this.linkHealthPlugin = linkHealthPlugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        if (args.length == 0) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Merci d'indiquer le joueur auquel vous souhaitez vous lier.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Le joueur indiqué n'est pas connecté");
            return true;
        }

        try {
            linkHealthPlugin.getHealthDatabase().linkPlayers(p, target);
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Votre vie est désormais liée à celle de " + target.getDisplayName());
            target.sendMessage(ChatColor.LIGHT_PURPLE + "Votre vie est désormais liée à celle de " + p.getDisplayName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
