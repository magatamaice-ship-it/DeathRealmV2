package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DeathRealmEntryEffect {

    private final DeathRealm plugin;

    public DeathRealmEntryEffect(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void play(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Location location = player.getLocation().clone();

        player.playSound(
                location,
                Sound.BLOCK_END_PORTAL_SPAWN,
                1.0f,
                0.6f
        );

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()
                        || !player.getWorld().getName()
                        .equalsIgnoreCase("DeathRealm")) {
                    cancel();
                    return;
                }

                if (tick % 5 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            player.getLocation().clone().add(0, 1, 0),
                            18,
                            0.5,
                            1.0,
                            0.5,
                            0.1
                    );

                    player.getWorld().spawnParticle(
                            Particle.SOUL,
                            player.getLocation().clone().add(0, 1, 0),
                            8,
                            0.4,
                            0.8,
                            0.4,
                            0.02
                    );
                }

                tick++;

                if (tick >= 60) {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
