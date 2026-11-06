
package id.deathrealm;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class DeathRealmCoordinateDisplay {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;

    private BukkitTask task;

    public DeathRealmCoordinateDisplay(
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
                this::updateCoordinates,
                20L,
                40L
        );
    }

    private void updateCoordinates() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {

            boolean viewerDead = timerManager.hasTimer(
                    viewer.getUniqueId()
            );

            List<String> coordinates = new ArrayList<>();

            // Koordinat hanya ditampilkan kepada pemain mati.
            if (!viewerDead) {
                viewer.sendActionBar(Component.empty());
                continue;
            }

            for (Player target : Bukkit.getOnlinePlayers()) {

                if (viewer.equals(target)) {
                    continue;
                }

                boolean targetDead = timerManager.hasTimer(
                        target.getUniqueId()
                );

                // Jangan tampilkan koordinat pemain hidup.
                if (!targetDead) {
                    continue;
                }

                if (!viewer.getWorld().equals(target.getWorld())) {
                    continue;
                }

                int x = target.getLocation().getBlockX();
                int y = target.getLocation().getBlockY();
                int z = target.getLocation().getBlockZ();

                coordinates.add(
                        target.getName() + ": "
                        + x + ", " + y + ", " + z
                );
            }

            if (coordinates.isEmpty()) {
                viewer.sendActionBar(Component.empty());
            } else {
                viewer.sendActionBar(
                        Component.text(String.join(" | ", coordinates))
                );
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
