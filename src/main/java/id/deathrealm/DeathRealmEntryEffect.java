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

                Location center = player.getLocation().clone();

                if (tick % 5 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            center.clone().add(0, 1, 0),
                            25,
                            0.6,
                            1.0,
                            0.6,
                            0.15
                    );

                    player.getWorld().spawnParticle(
                            Particle.SOUL,
                            center.clone().add(0, 1, 0),
                            12,
                            0.4,
                            0.8,
                            0.4,
                            0.03
                    );

                    double radius = 1.5 + (tick / 60.0);

                    for (int i = 0; i < 16; i++) {
                        double angle = Math.toRadians(i * 22.5);

                        Location circle = center.clone().add(
                                Math.cos(angle) * radius,
                                0.1,
                                Math.sin(angle) * radius
                        );

                        player.getWorld().spawnParticle(
                                Particle.END_ROD,
                                circle,
                                1,
                                0,
                                0.05,
                                0,
                                0
                        );
                    }
                }

                if (tick == 20) {
                    player.playSound(
                            center,
                            Sound.BLOCK_BEACON_POWER_SELECT,
                            1.0f,
                            0.8f
                    );
                }

                if (tick == 40) {
                    player.playSound(
                            center,
                            Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                            1.0f,
                            1.0f
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
