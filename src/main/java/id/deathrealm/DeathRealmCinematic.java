package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathRealmCinematic {

    private final DeathRealm plugin;

    public DeathRealmCinematic(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void playDeathAnimation(Player player, Runnable onComplete) {
        Location location = player.getLocation().clone();

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (tick == 0) {
                    player.getWorld().playSound(
                            location,
                            Sound.ENTITY_ALLAY_HURT,
                            1.5f,
                            0.5f
                    );
                }

                if (tick % 5 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.SOUL,
                            location.clone().add(0, 1, 0),
                            15,
                            0.5,
                            0.8,
                            0.5,
                            0.02
                    );

                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            location.clone().add(0, 1, 0),
                            25,
                            0.7,
                            1.0,
                            0.7,
                            0.1
                    );
                }

                if (tick == 40) {
                    player.getWorld().playSound(
                            location,
                            Sound.BLOCK_END_PORTAL_SPAWN,
                            1.5f,
                            0.7f
                    );
                }

                tick++;

                if (tick >= 100) {
                    cancel();
                    onComplete.run();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
