
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathRealmCamera {

    private final JavaPlugin plugin;

    public DeathRealmCamera(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void play(Player player, Runnable onComplete) {
        if (player == null || !player.isOnline()) {
            onComplete.run();
            return;
        }

        Location start = player.getLocation().clone();

        new BukkitRunnable() {
            private int tick = 0;
            private boolean completed = false;

            private void finish() {
                if (completed) return;

                completed = true;

                if (player.isOnline()) {
                    player.teleport(start);
                }

                onComplete.run();
            }

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    finish();
                    return;
                }

                if (tick >= 40) {
                    cancel();
                    finish();
                    return;
                }

                double progress = tick / 40.0;

                Location cameraLocation = start.clone().add(
                        0,
                        5 * progress,
                        0
                );

                cameraLocation.setYaw(start.getYaw());
                cameraLocation.setPitch(90);

                player.teleport(cameraLocation);

                tick++;
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
