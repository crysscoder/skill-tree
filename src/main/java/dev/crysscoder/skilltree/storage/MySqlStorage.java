package dev.crysscoder.skilltree.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import dev.crysscoder.skilltree.data.PlayerData;
import dev.crysscoder.skilltree.data.Task;
import dev.crysscoder.skilltree.enums.Skill;
import dev.crysscoder.skilltree.enums.Status;
import dev.crysscoder.skilltree.manager.ConfigManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MySqlStorage implements Storage {
    private HikariDataSource dataSource;
    private final JavaPlugin plugin;
    private final ExecutorService executorService = Executors.newFixedThreadPool(2);

    public MySqlStorage(JavaPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            setupDataSource();
        });
    }

    public void setupDataSource() {
        final String url = "jdbc:mysql://" + ConfigManager.MySQL.HOST + ":" +
                ConfigManager.MySQL.PORT + "/" + ConfigManager.MySQL.DATABASE;

        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(ConfigManager.MySQL.USER);
        config.setPassword(ConfigManager.MySQL.PASSWORD);
        config.setMaximumPoolSize(3);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
        initDatabase();
    }


    public void initDatabase() {
        CompletableFuture.runAsync(() -> {
            String playersSql = """
                        CREATE TABLE IF NOT EXISTS players (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            player_name VARCHAR(36) UNIQUE NOT NULL,
                            class VARCHAR(20),
                            progress INT DEFAULT 0
                        );
                    """;

            String tasksSql = """
                        CREATE TABLE IF NOT EXISTS tasks (
                            id INT AUTO_INCREMENT PRIMARY KEY,
                            player_id INT NOT NULL,
                            task_name VARCHAR(255) NOT NULL,
                            challenge_id VARCHAR(255) NOT NULL,
                            status VARCHAR(20) DEFAULT 'NOT_STARTED',
                            progress INT DEFAULT 0,
                            FOREIGN KEY (player_id) REFERENCES players(id) ON DELETE CASCADE
                        );
                    """;

            try (Connection conn = dataSource.getConnection();
                 Statement st = conn.createStatement()) {
                st.executeUpdate(playersSql);
                st.executeUpdate(tasksSql);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<PlayerData> getPlayer(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM players WHERE player_name = ?";
            try (final Connection conn = dataSource.getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, playerName);
                try (final ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerData(
                                rs.getInt("id"),
                                rs.getString("player_name"),
                                Skill.valueOf(rs.getString("class").toUpperCase()),
                                rs.getInt("progress")
                        );
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletableFuture<PlayerData> getPlayer(int id) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM players WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new PlayerData(
                                rs.getInt("id"),
                                rs.getString("player_name"),
                                Skill.valueOf(rs.getString("class").toUpperCase()),
                                rs.getInt("progress")
                        );
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, executorService);
    }

    @Override
    public void addPlayer(PlayerData player) {
        CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO players (player_name, class, progress) VALUES (?, ?, ?)";
            try (final Connection conn = dataSource.getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, player.getPlayerName());
                ps.setString(2, player.getSkill().name());
                ps.setInt(3, player.getProgress());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void removePlayer(int id) {
        CompletableFuture.runAsync(() -> {
            final String sql = "DELETE FROM players WHERE id = ?";
            try (final Connection conn = dataSource.getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void updatePlayer(PlayerData player) {
        CompletableFuture.runAsync(() -> {
            final String sql = "UPDATE players SET class = ?, progress = ? WHERE player_name = ?";
            try (final Connection conn = dataSource.getConnection();
                 final PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, player.getSkill().name());
                ps.setInt(2, player.getProgress());
                ps.setString(3, player.getPlayerName());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Task> getTask(int id) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM tasks WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new Task(
                                rs.getInt("id"),
                                rs.getInt("player_id"),
                                rs.getString("task_name"),
                                rs.getString("challenge_id"),
                                Status.valueOf(rs.getString("status").toUpperCase()),
                                rs.getInt("progress")
                        );
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        }, executorService);
    }

    @Override
    public CompletableFuture<List<Task>> getTasksByPlayer(int playerId) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM tasks WHERE player_id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    List<Task> tasks = new ArrayList<>();
                    while (rs.next()) {
                        tasks.add(new Task(
                                rs.getInt("id"),
                                rs.getInt("player_id"),
                                rs.getString("task_name"),
                                rs.getString("challenge_id"),
                                Status.valueOf(rs.getString("status").toUpperCase()),
                                rs.getInt("progress")
                        ));
                    }
                    return tasks;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<List<Task>> getTasksByStatus(int playerId, Status status) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT * FROM tasks WHERE player_id = ? AND status = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, playerId);
                ps.setString(2, status.name());
                try (ResultSet rs = ps.executeQuery()) {
                    List<Task> tasks = new ArrayList<>();
                    while (rs.next()) {
                        tasks.add(new Task(
                                rs.getInt("id"),
                                rs.getInt("player_id"),
                                rs.getString("task_name"),
                                rs.getString("challenge_id"),
                                Status.valueOf(rs.getString("status").toUpperCase()),
                                rs.getInt("progress")
                        ));
                    }
                    return tasks;
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void addTask(Task task) {
        CompletableFuture.runAsync(() -> {
            final String sql = "INSERT INTO tasks (player_id, task_name, challenge_id, status, progress) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, task.getPlayerId());
                ps.setString(2, task.getTaskName());
                ps.setString(3, task.getChallengeId());
                ps.setString(4, task.getStatus().name());
                ps.setInt(5, task.getProgress());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        task.setId(rs.getInt(1));
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void removeTask(int id) {
        CompletableFuture.runAsync(() -> {
            final String sql = "DELETE FROM tasks WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void updateTask(Task task) {
        CompletableFuture.runAsync(() -> {
            final String sql = "UPDATE tasks SET task_name = ?, status = ?, progress = ? WHERE id = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, task.getTaskName());
                ps.setString(2, task.getStatus().name());
                ps.setInt(3, task.getProgress());
                ps.setInt(4, task.getId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public CompletableFuture<Integer> countTasksByStatus(String status) {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT COUNT(*) AS count FROM tasks WHERE status = ?";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, status.toUpperCase());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt("count");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        }, executorService);
    }

    @Override
    public CompletableFuture<Integer> countPlayers() {
        return CompletableFuture.supplyAsync(() -> {
            final String sql = "SELECT COUNT(*) AS count FROM players";
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("count");
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return 0;
        }, executorService);
    }
}
