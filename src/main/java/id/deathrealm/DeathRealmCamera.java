
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
        Location start = player.getLocation().clone();
        Location target = start.clone().add(0, 5, 0);

        float originalWalkSpeed = player.getWalkSpeed();
        float originalFlySpeed = player.getFlySpeed();
        boolean originalInvulnerable = player.isInvulnerable();

        player.setInvulnerable(true);
        player.setWalkSpeed(0);
        player.setFlySpeed(0);

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (tick >= 40) {
                    cancel();

                    player.teleport(start);
                    player.setWalkSpeed(originalWalkSpeed);
                    player.setFlySpeed(originalFlySpeed);
                    player.setInvulnerable(originalInvulnerable);

                    onComplete.run();
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
