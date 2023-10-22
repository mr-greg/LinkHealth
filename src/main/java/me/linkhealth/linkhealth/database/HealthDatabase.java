package me.linkhealth.linkhealth.database;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Objects;
import java.util.UUID;

public class HealthDatabase {

    private final Connection connection;
    public HealthDatabase(String path) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + path);

        // Put this in a try to automatically close the statement instead of doing statement.close()
        try(Statement statement = connection.createStatement()){
            statement.execute("""
                     CREATE TABLE IF NOT EXISTS players (
                     uuid TEXT,
                     health DOUBLE NOT NULL DEFAULT 20,
                     linkedplayer TEXT)
                     """);
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()){
            connection.close();
        }
    }

    public void addPlayer(Player p) throws  SQLException{
        if (playerExists(p)) return;
        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO players (uuid, health) VALUES (?,?) ")){
            preparedStatement.setString(1, p.getUniqueId().toString());
            preparedStatement.setDouble(2, p.getHealth());
            preparedStatement.executeUpdate();
        }
    }

    // Returns true if player is in database
    public boolean playerExists(Player p) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        }
    }

    // Returns true if player is linked
    public boolean isPlayerLinked(Player p) throws SQLException {
        if (!playerExists(p)) {
            try {
                addPlayer(p);
                return false;
            }catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT linkedplayer FROM players WHERE uuid = ?")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            return resultSet.getString("linkedplayer") != null;
        }
    }

    // returns the uuid (string) of the linked player (if not linked, returns null)
    public String getLinkedPlayer(Player p) throws  SQLException {
        if (!playerExists(p)) {
            try {
                addPlayer(p);
                return null;
            }catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT uuid FROM players WHERE linkedplayer = ?")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getString("uuid");
        }
    }

    public void linkPlayers(Player casterPlayer, Player targetPlayer) throws SQLException{
        if (!playerExists(casterPlayer) || !playerExists(targetPlayer)) {
            try {
                addPlayer(casterPlayer);
                addPlayer(targetPlayer);
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET linkedplayer = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, targetPlayer.getUniqueId().toString());
            preparedStatement.setString(2, casterPlayer.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET linkedplayer = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, casterPlayer.getUniqueId().toString());
            preparedStatement.setString(2, targetPlayer.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }

    public void unlinkPlayers(Player p) throws SQLException{
        if (!playerExists(p)) {
            try {
                addPlayer(p);
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        String linkedPlayerUUID = getLinkedPlayer(p);

        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET linkedplayer = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, p.getUniqueId().toString());
            preparedStatement.setString(2, "");
            preparedStatement.executeUpdate();
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET linkedplayer = ? WHERE uuid = ?")) {
            preparedStatement.setString(1, linkedPlayerUUID);
            preparedStatement.setString(2, "");
            preparedStatement.executeUpdate();
        }
    }



    public double getHealth(Player p) throws  SQLException{
        if (!playerExists(p)) {
            try {
                addPlayer(p);
                return Double.NaN;
            }catch (SQLException e) {
                e.printStackTrace();
                return Double.NaN;
            }
        }
        double health = Double.NaN;
        try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT health FROM players WHERE uuid = ?")){
            preparedStatement.setString(1, p.getUniqueId().toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            health = resultSet.getDouble("health");
            return health;
        }
    }

    public void updateHealthDatabase(Player p, double health, boolean damage) throws SQLException{
        if (!playerExists(p)) {
            try {
                addPlayer(p);
                return;
            }catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        if (!isPlayerLinked(p)) return;

        if (health > 20) health = 20;
        if (health < 0) health = 0;

        String linkedPlayerUUID = getLinkedPlayer(p);
        Player linkedPlayer = Bukkit.getPlayer(UUID.fromString(linkedPlayerUUID));


        if (linkedPlayer != null && linkedPlayer.isOnline()) {
            linkedPlayer.setHealth(health);
            p.setHealth(health);
            if (health <= 0) {
                linkedPlayer.damage(50);
                p.damage(50);
            }
        } else {
            p.setHealth(health);
        }


        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET health = ? WHERE uuid = ?")) {
            preparedStatement.setDouble(1, health);
            preparedStatement.setString(2, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }

        try (PreparedStatement preparedStatement = connection.prepareStatement("UPDATE players SET health = ? WHERE linkedplayer = ?")) {
            preparedStatement.setDouble(1, health);
            preparedStatement.setString(2, p.getUniqueId().toString());
            preparedStatement.executeUpdate();
        }
    }


}
