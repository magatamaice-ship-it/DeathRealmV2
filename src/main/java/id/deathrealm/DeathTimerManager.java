package id.deathrealm;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathTimerManager {

    private static final long DEATH_TIME = 25 * 60 * 1000L;

    private final DeathRealm plugin;
    private final File file;
    private final Map<UUID, Long> timers = new HashMap<>();

    public DeathTimerManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "timers.yml");

        loadTimers();
    }

    public void startTimer(UUID playerId) {
        long endTime = System.currentTimeMillis() + DEATH_TIME;

        timers.put(playerId, endTime);
        saveTimers();
    }

    public boolean hasTimer(UUID playerId) {
        return timers.containsKey(playerId);
    }

    public long getRemainingTime(UUID playerId) {
        Long endTime = timers.get(playerId);

        if (endTime == null) {
            return 0L;
        }

        return Math.max(0L, endTime - System.currentTimeMillis());
    }

    public boolean isExpired(UUID playerId) {
        return hasTimer(playerId)
                && getRemainingTime(playerId) <= 0L;
    }

    public void removeTimer(UUID playerId) {
        if (timers.remove(playerId) != null) {
            saveTimers();
        }
    }

    public void saveTimers() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Long> entry : timers.entrySet()) {
            config.set(
                    entry.getKey().toString(),
                    entry.getValue()
            );
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan timers.yml: "
                    + exception.getMessage()
            );
        }
    }

    private void loadTimers() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(key);
                long endTime = config.getLong(key);

                timers.put(playerId, endTime);
            } catch (IllegalArgumentException exception) {
                plugin.getLogger().warning(
                        "UUID tidak valid di timers.yml: " + key
                );
            }
        }

        plugin.getLogger().info(
                "Berhasil memuat " + timers.size()
                + " timer kematian."
        );
    }
}
