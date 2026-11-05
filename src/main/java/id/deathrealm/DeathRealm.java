package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class DeathRealmIsland {

    private final DeathRealm plugin;
    private final World world;

    private final List<BlockData> blocks = new ArrayList<>();

    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final int SURFACE_Y = 90;
    private static final int RADIUS = 80;
    private static final int DEPTH = 24;

    public DeathRealmIsland(DeathRealm plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void generate() {
        generate(null);
    }

    public void generate(Runnable onComplete) {
        if (world == null) {
            plugin.getLogger().warning(
                    "Dunia DeathRealm belum tersedia!"
            );
            return;
        }

        if (world.getBlockAt(
                CENTER_X, SURFACE_Y, CENTER_Z
        ).getType() != Material.AIR) {

            plugin.getLogger().info(
                    "Pulau DeathRealm sudah tersedia."
            );

            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        prepareBlocks();

        new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                int limit = Math.min(
                        index + 500,
                        blocks.size()
                );

                while (index < limit) {
                    BlockData data = blocks.get(index++);

                    world.getBlockAt(
                            data.x,
                            data.y,
                            data.z
                    ).setType(data.material, false);
                }

                if (index >= blocks.size()) {
                    plugin.getLogger().info(
                            "Pulau DeathRealm selesai dibuat!"
                    );

                    cancel();

                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    private void prepareBlocks() {
        for (int y = 0; y <= DEPTH; y++) {

            double progress = (double) y / DEPTH;

            int currentRadius = Math.max(
                    2,
                    (int) (RADIUS * (1.0 - progress))
            );

            for (int x = -currentRadius;
                 x <= currentRadius; x++) {

                for (int z = -currentRadius;
                     z <= currentRadius; z++) {

                    double distance = Math.sqrt(
                            x * x + z * z
                    );

                    if (distance > currentRadius) {
                        continue;
                    }

                    Material material;

                    if (y == 0) {
                        material = Material.GRASS_BLOCK;
                    } else if (y <= 3) {
                        material = Material.DIRT;
                    } else if (y <= 7) {
                        material = Material.STONE;
                    } else if (y <= 15) {
                        material = Material.DEEPSLATE;
                    } else {
                        material = Material.DEEPSLATE_BRICKS;
                    }

                    blocks.add(new BlockData(
                            CENTER_X + x,
                            SURFACE_Y - y,
                            CENTER_Z + z,
                            material
                    ));
                }
            }
        }
    }

    private static class BlockData {
        private final int x;
        private final int y;
        private final int z;
        private final Material material;

        private BlockData(
                int x,
                int y,
                int z,
                Material material
        ) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.material = material;
        }
    }
}
