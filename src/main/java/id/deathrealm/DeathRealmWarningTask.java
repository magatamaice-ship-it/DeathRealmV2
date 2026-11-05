package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathRealmWarningTask {

    private static final long WARNING_TIME = 5 * 60 * 1000L;

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;

    private final Set<UUID> warnedPlayers = new HashSet<>();

    private BukkitTask task;

    public DeathRealmWarningTask(
            DeathRealm plugin,
            DeathTimerManager timerManager
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
    }

    public void start() {
        if (task != null) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkWarnings,
                20L,
                20L
        );
    }

    public void checkPlayer(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!timerManager.hasTimer(uuid)) {
            warnedPlayers.remove(uuid);
            return;
        }

        if (timerManager.getRemainingTime(uuid) <= WARNING_TIME
                && warnedPlayers.add(uuid)) {

            plugin.getMessages().sendAlmostExpiredMessage(player);
        }
    }

    private void checkWarnings() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayer(player);
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        warnedPlayers.clear();
    }
}
