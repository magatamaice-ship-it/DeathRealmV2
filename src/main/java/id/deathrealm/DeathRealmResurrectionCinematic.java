package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathRealmResurrectionCinematic {

    private final DeathRealm plugin;

    public DeathRealmResurrectionCinematic(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void play(Player player, Runnable onComplete) {
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
                            Sound.BLOCK_BEACON_POWER_SELECT,
                            1.5f,
                            0.7f
                    );
                }

                if (tick % 5 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            location.clone().add(0, 1, 0),
                            20,
                            0.5,
                            0.8,
                            0.5,
                            0.03
                    );

                    player.getWorld().spawnParticle(
                            Particle.SOUL_FIRE_FLAME,
                            location.clone().add(0, 1, 0),
                            12,
                            0.4,
                            0.8,
                            0.4,
                            0.02
                    );
                }

                tick++;

                if (tick >= 100) {
                    cancel();

                    player.getWorld().playSound(
                            player.getLocation(),
                            Sound.ITEM_TOTEM_USE,
                            1.5f,
                            1.0f
                    );

                    onComplete.run();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
