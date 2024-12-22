package com.github.zyypj.bosses.database;

import com.github.zyypj.bosses.BossesPlugin;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class DatabaseManager {

    private final BossesPlugin plugin;
    private final Map<String, PlayerStats> cache = new ConcurrentHashMap<>();
    private Connection connection;

    public DatabaseManager(BossesPlugin plugin) {
        this.plugin = plugin;
        connect();
        startSaveTask();
    }

    private void connect() {

        try {
            ConfigurationSection mysqlConfig = plugin.getConfig().getConfigurationSection("mysql");
            if (mysqlConfig == null) {
                throw new IllegalArgumentException("Configuração MySQL não encontrada na config.yml");
            }

            String host = mysqlConfig.getString("host", "localhost");
            int port = mysqlConfig.getInt("port", 3306);
            String database = mysqlConfig.getString("database", "minecraft");
            String username = mysqlConfig.getString("user", "root");
            String password = mysqlConfig.getString("password", "");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
            connection = DriverManager.getConnection(url, username, password);
            plugin.debug("§aConexão com o banco de dados estabelecida!", true);

            assert connection != null;
            try (PreparedStatement stmt = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_stats (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "bosses_placed INT DEFAULT 0, " +
                            "bosses_killed INT DEFAULT 0)");
            ) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.debug("§cErro ao conectar-se ao banco de dados: " + e.getMessage(), false);
        }
    }

    private void testConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                plugin.debug("§cConexão com o banco de dados perdida. Tentando reconectar...", true);
                connect();
            }
        } catch (SQLException e) {
            plugin.debug("§cErro ao verificar ou reconectar ao banco de dados: " + e.getMessage(), false);
        }
    }

    public void addBossPlaced(Player player) {
        testConnection();
        cache.computeIfAbsent(player.getUniqueId().toString(), uuid -> new PlayerStats()).addBossPlaced();
    }

    public void addBossKilled(Player player) {
        testConnection();
        cache.computeIfAbsent(player.getUniqueId().toString(), uuid -> new PlayerStats()).addBossKilled();
    }

    public PlayerStats getPlayerStats(Player player) {
        testConnection();
        String uuid = player.getUniqueId().toString();
        PlayerStats cachedStats = cache.getOrDefault(uuid, new PlayerStats());

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT bosses_placed, bosses_killed FROM player_stats WHERE uuid = ?");
        ) {
            stmt.setString(1, uuid);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int bossesPlaced = rs.getInt("bosses_placed");
                    int bossesKilled = rs.getInt("bosses_killed");

                    return cachedStats.merge(new PlayerStats(bossesPlaced, bossesKilled));
                }
            }
        } catch (SQLException e) {
            plugin.debug("§cErro ao buscar estatísticas do jogador: " + e.getMessage(), false);
        }

        return cachedStats;
    }

    private void startSaveTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                testConnection();
                saveCacheToDatabase();
            }
        }.runTaskTimerAsynchronously(plugin, 20L * 60 * 5, 20L * 60 * 5);
    }

    private void saveCacheToDatabase() {
        testConnection();
        try {
            for (Map.Entry<String, PlayerStats> entry : cache.entrySet()) {
                String uuid = entry.getKey();
                PlayerStats stats = entry.getValue();

                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO player_stats (uuid, bosses_placed, bosses_killed) " +
                                "VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE " +
                                "bosses_placed = bosses_placed + VALUES(bosses_placed), " +
                                "bosses_killed = bosses_killed + VALUES(bosses_killed)");
                ) {
                    stmt.setString(1, uuid);
                    stmt.setInt(2, stats.getBossesPlaced());
                    stmt.setInt(3, stats.getBossesKilled());
                    stmt.executeUpdate();
                }
            }

            cache.clear();

        } catch (SQLException e) {
            plugin.debug("Erro ao salvar cache no banco de dados: " + e.getMessage(), true);
        }
    }

    public void closeConnection() {
        try {
            if (connection !=null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.debug("§cErro ao fechar conexão com o banco de dados: " + e.getMessage(), false);
        }
    }

    @Getter
    public static class PlayerStats {
        private int bossesPlaced;
        private int bossesKilled;

        public PlayerStats() {
            this(0, 0);
        }

        public PlayerStats(int bossesPlaced, int bossesKilled) {
            this.bossesPlaced = bossesPlaced;
            this.bossesKilled = bossesKilled;
        }

        public void addBossPlaced() {
            bossesPlaced++;
        }

        public void addBossKilled() {
            bossesKilled++;
        }

        public PlayerStats merge(PlayerStats other) {
            this.bossesPlaced += other.bossesPlaced;
            this.bossesKilled += other.bossesKilled;
            return this;
        }
    }
}
