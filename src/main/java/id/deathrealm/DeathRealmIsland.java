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
                int limit = Math.min(index + 500, blocks.size());

                while (index < limit) {
                    BlockData data = blocks.get(index++);

                    world.getBlockAt(data.x, data.y, data.z)
                            .setType(data.material, false);
                }

                if (index >= blocks.size()) {
                    plugin.getLogger().info(
                            "Daratan utama DeathRealm selesai dibuat!"
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
        for (int x = -RADIUS; x <= RADIUS; x++) {
            for (int z = -RADIUS; z <= RADIUS; z++) {

                double distance = Math.sqrt(x * x + z * z);

                if (distance > RADIUS) {
                    continue;
                }

                blocks.add(new BlockData(
                        x, SURFACE_Y, z, Material.STONE
                ));

                if (distance < RADIUS - 5) {
                    blocks.add(new BlockData(
                            x, SURFACE_Y - 1, z,
                            Material.DEEPSLATE
                    ));

                    blocks.add(new BlockData(
                            x, SURFACE_Y - 2, z,
                            Material.DEEPSLATE
                    ));

                    blocks.add(new BlockData(
                            x, SURFACE_Y - 3, z,
                            Material.COBBLED_DEEPSLATE
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
