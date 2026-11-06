
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathRealmBossBar {

    private static final long DEATH_TIME = 25 * 60 * 1000L;

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;

    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    private BukkitTask updateTask;

    public DeathRealmBossBar(
            DeathRealm plugin,
            DeathTimerManager timerManager
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
    }

    public void start() {
        if (updateTask != null) {
            return;
        }

        updateTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::updateAll,
                20L,
                20L
        );
    }

    public void show(Player player) {
        if (!timerManager.hasTimer(player.getUniqueId())) {
            return;
        }

        UUID uuid = player.getUniqueId();

        BossBar bar = bossBars.computeIfAbsent(
                uuid,
                key -> Bukkit.createBossBar(
                        "ALAMKACUNG",
                        BarColor.PURPLE,
                        BarStyle.SOLID
                )
        );

        if (!bar.getPlayers().contains(player)) {
            bar.addPlayer(player);
        }

        updatePlayer(player, bar);
    }

    public void hide(Player player) {
        BossBar bar = bossBars.remove(player.getUniqueId());

        if (bar != null) {
            bar.removeAll();
        }
    }

    private void updateAll() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();

            if (!timerManager.hasTimer(uuid)
                    || !player.getWorld().getName()
                    .equalsIgnoreCase("DeathRealm")) {
                hide(player);
                continue;
            }

            show(player);
        }
    }

    private void updatePlayer(Player player, BossBar bar) {
        UUID uuid = player.getUniqueId();

        if (timerManager.isTrialUnlocked(uuid)) {
            int collected = timerManager.getFragmentCount(uuid);

            bar.setTitle(
                    "§5ALAMKACUNG §8| §dFragmen Jiwa: §f"
                    + collected + "/3"
            );

            bar.setColor(BarColor.PURPLE);

            bar.setProgress(
                    Math.max(0.0, Math.min(1.0, collected / 3.0))
            );

            return;
        }

        long remaining = timerManager.getRemainingTime(uuid);
        long seconds = remaining / 1000L;

        long minutes = seconds / 60L;
        long remainingSeconds = seconds % 60L;

        String time = String.format(
                "%02d:%02d",
                minutes,
                remainingSeconds
        );

        double progress = Math.max(
                0.0,
                Math.min(1.0, (double) remaining / DEATH_TIME)
        );

        bar.setTitle(
                "§5ALAMKACUNG §8| §fWaktu tersisa: §d" + time
        );

        bar.setColor(BarColor.PURPLE);
        bar.setProgress(progress);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        for (BossBar bar : bossBars.values()) {
            bar.removeAll();
        }

        bossBars.clear();
    }
}
