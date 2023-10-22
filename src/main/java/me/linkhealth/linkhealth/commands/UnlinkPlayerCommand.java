package me.linkhealth.linkhealth.commands;

import me.linkhealth.linkhealth.LinkHealth;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class UnlinkPlayerCommand implements CommandExecutor {

    private final LinkHealth linkHealthPlugin;

    public UnlinkPlayerCommand(LinkHealth linkHealthPlugin) {
        this.linkHealthPlugin = linkHealthPlugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        try {
            linkHealthPlugin.getHealthDatabase().unlinkPlayers(p);
            p.sendMessage(ChatColor.LIGHT_PURPLE + "Vous n'êtes plus lié à votre partenaire !");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
