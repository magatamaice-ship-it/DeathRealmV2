
package id.deathrealm;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeathTimerManager {

    private static final long DEATH_TIME = 25 * 60 * 1000L;

    public static final String ANCIENT = "ancient";
    public static final String CURSED = "cursed";
    public static final String LAST = "last";

    private final DeathRealm plugin;
    private final File file;

    private final Map<UUID, Long> timers = new HashMap<>();
    private final Set<UUID> unlockedTrials = new HashSet<>();
    private final Map<UUID, Set<String>> fragments = new HashMap<>();

    public DeathTimerManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "timers.yml");

        loadTimers();
    }

    public void startTimer(UUID playerId) {
        // Setiap kematian memulai siklus baru.
        timers.put(
                playerId,
                System.currentTimeMillis() + DEATH_TIME
        );

        unlockedTrials.remove(playerId);
        fragments.remove(playerId);

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

        return Math.max(
                0L,
                endTime - System.currentTimeMillis()
        );
    }

    public boolean isExpired(UUID playerId) {
        return hasTimer(playerId)
                && getRemainingTime(playerId) <= 0L;
    }

    public void unlockTrial(UUID playerId) {
        if (!hasTimer(playerId)
                || !isExpired(playerId)) {
            return;
        }

        if (unlockedTrials.add(playerId)) {
            saveTimers();
        }
    }

    public boolean isTrialUnlocked(UUID playerId) {
        return unlockedTrials.contains(playerId);
    }

    public boolean addFragment(UUID playerId, String fragment) {
        if (!hasTimer(playerId)
                || !isTrialUnlocked(playerId)
                || !isValidFragment(fragment)) {
            return false;
        }

        Set<String> collected = fragments.computeIfAbsent(
                playerId,
                key -> new HashSet<>()
        );

        if (!collected.add(fragment)) {
            return false;
        }

        saveTimers();
        return true;
    }

    public boolean hasFragment(UUID playerId, String fragment) {
        return fragments.getOrDefault(
                playerId,
                new HashSet<>()
        ).contains(fragment);
    }

    public int getFragmentCount(UUID playerId) {
        return fragments.getOrDefault(
                playerId,
                new HashSet<>()
        ).size();
    }

    public void resetCycle(UUID playerId) {
        timers.remove(playerId);
        unlockedTrials.remove(playerId);
        fragments.remove(playerId);

        saveTimers();
    }

    public void removeTimer(UUID playerId) {
        resetCycle(playerId);
    }

    public void saveTimers() {
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe(
                    "Gagal membuat folder data DeathRealm!"
            );
            return;
        }

        YamlConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Long> entry : timers.entrySet()) {
            config.set(
                    "timers." + entry.getKey(),
                    entry.getValue()
            );
        }

        for (UUID uuid : unlockedTrials) {
            config.set(
                    "trials." + uuid + ".unlocked",
                    true
            );
        }

        for (Map.Entry<UUID, Set<String>> entry : fragments.entrySet()) {
            for (String fragment : entry.getValue()) {
                config.set(
                        "trials." + entry.getKey()
                                + ".fragments." + fragment,
                        true
                );
            }
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
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe(
                    "Gagal membuat folder data DeathRealm!"
            );
            return;
        }

        if (!file.exists()) {
            return;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        // Memuat format baru.
        ConfigurationSection timerSection =
                config.getConfigurationSection("timers");

        if (timerSection != null) {
            for (String key : timerSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long endTime = timerSection.getLong(key);

                    if (endTime > 0L) {
                        timers.put(uuid, endTime);
                    }
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning(
                            "UUID timer tidak valid: " + key
                    );
                }
            }
        }

        // Kompatibilitas dengan timers.yml format lama.
        for (String key : config.getKeys(false)) {
            if (key.equals("timers") || key.equals("trials")) {
                continue;
            }

            try {
                UUID uuid = UUID.fromString(key);
                long endTime = config.getLong(key);

                if (endTime > 0L) {
                    timers.putIfAbsent(uuid, endTime);
                }
            } catch (IllegalArgumentException ignored) {
                // Abaikan key yang bukan UUID.
            }
        }

        // Memuat status ujian dan fragmen.
        ConfigurationSection trialSection =
                config.getConfigurationSection("trials");

        if (trialSection != null) {
            for (String key : trialSection.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);

                    if (trialSection.getBoolean(
                            key + ".unlocked", false)) {
                        unlockedTrials.add(uuid);
                    }

                    Set<String> collected = new HashSet<>();

                    for (String fragment :
                            new String[]{ANCIENT, CURSED, LAST}) {

                        if (trialSection.getBoolean(
                                key + ".fragments." + fragment,
                                false)) {
                            collected.add(fragment);
                        }
                    }

                    if (!collected.isEmpty()) {
                        fragments.put(uuid, collected);
                    }

                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning(
                            "UUID ujian tidak valid: " + key
                    );
                }
            }
        }

        plugin.getLogger().info(
                "Berhasil memuat " + timers.size()
                + " timer kematian."
        );
    }

    private boolean isValidFragment(String fragment) {
        return ANCIENT.equals(fragment)
                || CURSED.equals(fragment)
                || LAST.equals(fragment);
    }
}
