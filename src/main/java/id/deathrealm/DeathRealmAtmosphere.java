package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class DeathRealmAtmosphere {

    private final DeathRealm plugin;
    private final Random random = new Random();

    private BukkitRunnable task;

    private static final Location ALTAR_LOCATION =
            new Location(null, 0, 94, -45);

    public DeathRealmAtmosphere(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.getWorld().getName().equals("DeathRealm")) {
                        continue;
                    }

                    World world = player.getWorld();
                    Location center = player.getLocation();

                    // Kabut di sekitar pemain
                    for (int i = 0; i < 4; i++) {
                        double x = random.nextDouble() * 16 - 8;
                        double y = random.nextDouble() * 5 - 1;
                        double z = random.nextDouble() * 16 - 8;

                        Location fog = center.clone().add(x, y, z);

                        world.spawnParticle(
                                Particle.DUST,
                                fog,
                                2,
                                0.8,
                                0.5,
                                0.8,
                                0,
                                new Particle.DustOptions(
                                        Color.fromRGB(100, 90, 130),
                                        2.0F
                                )
                        );
                    }

                    world.spawnParticle(
                            Particle.ASH,
                            center.clone().add(0, 2, 0),
                            3,
                            5,
                            1,
                            5,
                            0.01
                    );

                    // Energi altar
                    Location altar = new Location(
                            world,
                            0.5,
                            94,
                            -44.5
                    );

                    if (center.distanceSquared(altar) <= 2500) {
                        world.spawnParticle(
                                Particle.PORTAL,
                                altar,
                                12,
                                1.5,
                                1.0,
                                1.5,
                                0.5
                        );

                        world.spawnParticle(
                                Particle.SOUL,
                                altar,
                                5,
                                0.7,
                                0.8,
                                0.7,
                                0.02
                        );
                    }
                }
            }
        };

        task.runTaskTimer(plugin, 10L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
