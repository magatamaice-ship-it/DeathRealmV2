package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class DeathRealmTimerTask {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmResurrection resurrection;

    private BukkitTask task;

    public DeathRealmTimerTask(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmResurrection resurrection
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.resurrection = resurrection;
    }

    public void start() {
        if (task != null) {
            return;
        }

        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::checkTimers,
                20L,
                20L
        );
    }

    private void checkTimers() {
        for (Player player : Bukkit.getOnlinePlayers()) {

            if (!timerManager.hasTimer(player.getUniqueId())) {
                continue;
            }

            if (timerManager.isExpired(player.getUniqueId())) {
                resurrection.resurrect(player);
            }
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
