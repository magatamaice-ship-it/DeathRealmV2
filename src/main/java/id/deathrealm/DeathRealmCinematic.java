package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class DeathRealmCinematic {

    private final DeathRealm plugin;

    public DeathRealmCinematic(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void playDeathAnimation(
            Player player,
            Runnable onComplete
    ) {
        Location location = player.getLocation().clone();

        boolean previousInvulnerable = player.isInvulnerable();
        float previousWalkSpeed = player.getWalkSpeed();
        float previousFlySpeed = player.getFlySpeed();

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

                if (tick == 0) {
                    player.getWorld().playSound(
                            location,
                            Sound.ENTITY_ALLAY_HURT,
                            1.5f,
                            0.5f
                    );
                }

                double rise = Math.min(tick / 35.0, 2.5);

                Location soulLocation = location.clone().add(
                        0,
                        1 + rise,
                        0
                );

                if (tick % 3 == 0) {
                    player.getWorld().spawnParticle(
                            Particle.SOUL,
                            soulLocation,
                            8,
                            0.25,
                            0.3,
                            0.25,
                            0.02
                    );

                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            soulLocation,
                            5,
                            0.2,
                            0.3,
                            0.2,
                            0.01
                    );
                }

                if (tick % 5 == 0) {
                    for (int i = 0; i < 12; i++) {
                        double angle = Math.toRadians(i * 30);
                        double radius = 1.0 + tick / 100.0;

                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;

                        Location circle = location.clone().add(
                                x,
                                0.5 + rise,
                                z
                        );

                        player.getWorld().spawnParticle(
                                Particle.PORTAL,
                                circle,
                                3,
                                0.05,
                                0.05,
                                0.05,
                                0.02
                        );
                    }
                }

                if (tick == 40) {
                    player.getWorld().playSound(
                            location,
                            Sound.BLOCK_END_PORTAL_SPAWN,
                            1.5f,
                            0.7f
                    );
                }

                if (tick == 80) {
                    player.getWorld().playSound(
                            location,
                            Sound.BLOCK_BEACON_DEACTIVATE,
                            1.5f,
                            0.6f
                    );
                }

                tick++;

                if (tick >= 100) {
                    cancel();

                    if (player.isOnline()) {
                        player.setInvulnerable(previousInvulnerable);
                        player.setWalkSpeed(previousWalkSpeed);
                        player.setFlySpeed(previousFlySpeed);

                        onComplete.run();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
