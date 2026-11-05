
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathRealmResurrectionCinematic {

    private final DeathRealm plugin;
    private final DeathRealmCamera camera;

    public DeathRealmResurrectionCinematic(DeathRealm plugin) {
        this.plugin = plugin;
        this.camera = new DeathRealmCamera(plugin);
    }

    public void play(Player player, Runnable onComplete) {

        Location location = player.getLocation().clone();

        player.setInvulnerable(true);
        player.setWalkSpeed(0);
        player.setFlySpeed(0);

        player.getWorld().playSound(
                location,
                Sound.BLOCK_BEACON_POWER_SELECT,
                1.5f,
                0.7f
        );

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {

                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (tick % 5 == 0) {
                    player.getWorld().playSound(
                            player.getLocation(),
                            Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                            0.8f,
                            1.0f + (tick / 100.0f)
                    );
                }

                tick++;

                if (tick >= 20) {
                    cancel();

                    camera.play(player, () -> {
                        if (!player.isOnline()) {
                            return;
                        }

                        player.getWorld().playSound(
                                player.getLocation(),
                                Sound.ITEM_TOTEM_USE,
                                1.5f,
                                1.0f
                        );

                        onComplete.run();
                    });
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
