
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathRealmTimerTask {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;

    private final Set<UUID> processed = new HashSet<>();

    private BukkitTask task;

    public DeathRealmTimerTask(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmResurrection resurrection
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
                this::checkTimers,
                20L,
                20L
        );
    }

    private void checkTimers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (!timerManager.hasTimer(uuid)) {
                processed.remove(uuid);
                continue;
            }

            if (!timerManager.isExpired(uuid)) {
                continue;
            }

            if (processed.contains(uuid)) {
                continue;
            }

            timerManager.unlockTrial(uuid);

            if (timerManager.isTrialUnlocked(uuid)) {
                processed.add(uuid);

                player.sendTitle(
                        "§5UJIAN KEBANGKITAN",
                        "§dKumpulkan 3 Fragmen Jiwa",
                        10,
                        70,
                        20
                );

                player.sendMessage(
                        "§5[ALAMKACUNG] §dUjian kebangkitanmu telah terbuka!"
                );

                player.sendMessage(
                        "§7Temukan tiga Fragmen Jiwa untuk kembali hidup."
                );
            }
        }
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }

        processed.clear();
    }
}
