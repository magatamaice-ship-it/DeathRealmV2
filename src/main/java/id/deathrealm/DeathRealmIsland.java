
package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeathRealmIsland {

    private final DeathRealm plugin;
    private final World world;
    private final NamespacedKey generatedKey;

    private final List<BlockDataEntry> blocks = new ArrayList<>();
    private final List<PlantData> plants = new ArrayList<>();

    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final int SURFACE_Y = 90;
    private static final int RADIUS = 80;
    private static final int DEPTH = 24;

    private final Random random = new Random(2026);
    private boolean generating = false;

    public DeathRealmIsland(DeathRealm plugin, World world) {
        this.plugin = plugin;
        this.world = world;
        this.generatedKey = new NamespacedKey(
                plugin,
                "island-generated"
        );
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

        if (world.getPersistentDataContainer().has(
                generatedKey,
                PersistentDataType.BYTE
        )) {
            plugin.getLogger().info(
                    "Pulau DeathRealm sudah selesai dibuat."
            );

            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }

        if (generating) {
            plugin.getLogger().warning(
                    "Pembuatan pulau DeathRealm sedang berlangsung."
            );
            return;
        }

        generating = true;

        blocks.clear();
        plants.clear();

        prepareBlocks();

        new BukkitRunnable() {
            private int index = 0;
            private int plantIndex = 0;

            @Override
            public void run() {
                int limit = Math.min(
                        index + 500,
                        blocks.size()
                );

                while (index < limit) {
                    BlockDataEntry data = blocks.get(index++);

                    world.getBlockAt(
                            data.x,
                            data.y,
                            data.z
                    ).setType(data.material, false);
                }

                if (index >= blocks.size()) {
                    int plantLimit = Math.min(
                            plantIndex + 100,
                            plants.size()
                    );

                    while (plantIndex < plantLimit) {
                        PlantData plant = plants.get(plantIndex++);

                        Block ground = world.getBlockAt(
                                plant.x,
                                SURFACE_Y,
                                plant.z
                        );

                        Block above = ground.getRelative(0, 1, 0);

                        if (ground.getType() == Material.GRASS_BLOCK
                                && above.getType() == Material.AIR) {
                            above.setType(plant.material, false);
                        }
                    }

                    if (plantIndex >= plants.size()) {
                        world.getPersistentDataContainer().set(
                                generatedKey,
                                PersistentDataType.BYTE,
                                (byte) 1
                        );

                        world.save();

                        generating = false;

                        plugin.getLogger().info(
                                "Pulau DeathRealm dan vegetasi berhasil dibuat!"
                        );

                        cancel();

                        if (onComplete != null) {
                            onComplete.run();
                        }
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

                    blocks.add(new BlockDataEntry(
                            CENTER_X + x,
                            SURFACE_Y - y,
                            CENTER_Z + z,
                            material
                    ));
                }
            }
        }

        preparePlants();
    }

    private void preparePlants() {
        Material[] vegetation = {
                Material.DEAD_BUSH,
                Material.DEAD_BUSH,
                Material.FERN,
                Material.SHORT_GRASS,
                Material.BROWN_MUSHROOM,
                Material.RED_MUSHROOM
        };

        for (int i = 0; i < 350; i++) {
            int x = random.nextInt(141) - 70;
            int z = random.nextInt(141) - 70;

            if (Math.sqrt(x * x + z * z) > 70) {
                continue;
            }

            Material plant = vegetation[
                    random.nextInt(vegetation.length)
            ];

            plants.add(new PlantData(
                    x,
                    z,
                    plant
            ));
        }
    }

    private static class BlockDataEntry {
        private final int x;
        private final int y;
        private final int z;
        private final Material material;

        private BlockDataEntry(
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

    private static class PlantData {
        private final int x;
        private final int z;
        private final Material material;

        private PlantData(
                int x,
                int z,
                Material material
        ) {
            this.x = x;
            this.z = z;
            this.material = material;
        }
    }
}
