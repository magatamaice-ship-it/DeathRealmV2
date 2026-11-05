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

        boolean previousAllowFlight = player.getAllowFlight();
        boolean previousFlying = player.isFlying();
        boolean previousInvulnerable = player.isInvulnerable();

        player.setInvulnerable(true);
        player.setWalkSpeed(0);
        player.setFlySpeed(0);

        new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    restorePlayer();
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
                    double intensity = 0.5 + (tick / 100.0);

                    player.getWorld().spawnParticle(
                            Particle.END_ROD,
                            location.clone().add(0, 1, 0),
                            20 + tick / 5,
                            intensity,
                            0.8,
                            intensity,
                            0.03
                    );

                    player.getWorld().spawnParticle(
                            Particle.SOUL_FIRE_FLAME,
                            location.clone().add(0, 1, 0),
                            12,
                            intensity,
                            0.8,
                            intensity,
                            0.02
                    );

                    player.getWorld().spawnParticle(
                            Particle.PORTAL,
                            location.clone().add(0, 1, 0),
                            15,
                            0.7,
                            1.0,
                            0.7,
                            0.5
                    );
                }

                if (tick == 40) {
                    player.getWorld().playSound(
                            location,
                            Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                            1.5f,
                            1.0f
                    );
                }

                if (tick == 80) {
                    player.getWorld().playSound(
                            location,
                            Sound.BLOCK_BEACON_ACTIVATE,
                            1.5f,
                            1.2f
                    );
                }

                tick++;

                if (tick >= 100) {
                    cancel();

                    restorePlayer();

                    player.getWorld().playSound(
                            player.getLocation(),
                            Sound.ITEM_TOTEM_USE,
                            1.5f,
                            1.0f
                    );

                    onComplete.run();
                }
            }

            private void restorePlayer() {
                if (!player.isOnline()) {
                    return;
                }

                player.setInvulnerable(previousInvulnerable);
                player.setWalkSpeed(0.2f);
                player.setFlySpeed(0.1f);
                player.setAllowFlight(previousAllowFlight);

                if (previousAllowFlight) {
                    player.setFlying(previousFlying);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
