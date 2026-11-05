package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class DeathRealmAtmosphere {

    private final DeathRealm plugin;
    private final Random random = new Random();

    private BukkitTask particleTask;
    private BukkitTask soundTask;

    public DeathRealmAtmosphere(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (particleTask != null) {
            return;
        }

        particleTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::spawnAtmosphere,
                40L,
                40L
        );

        soundTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::playAmbientSound,
                200L,
                400L
        );
    }

    private void spawnAtmosphere() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equalsIgnoreCase("DeathRealm")) {
                continue;
            }

            player.getWorld().spawnParticle(
                    Particle.ASH,
                    player.getLocation().add(
                            random.nextDouble() * 12 - 6,
                            random.nextDouble() * 4,
                            random.nextDouble() * 12 - 6
                    ),
                    4,
                    1.5,
                    1.0,
                    1.5,
                    0.01
            );
        }
    }

    private void playAmbientSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().getName().equalsIgnoreCase("DeathRealm")) {
                continue;
            }

            if (random.nextInt(100) < 40) {
                player.playSound(
                        player.getLocation(),
                        Sound.AMBIENT_SOUL_SAND_VALLEY_MOOD,
                        0.5f,
                        0.7f
                );
            }
        }
    }

    public void stop() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        if (soundTask != null) {
            soundTask.cancel();
            soundTask = null;
        }
    }
}
