package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class DeathRealmDeadCity {

    private final World world;
    private final Random random = new Random(7821);

    public DeathRealmDeadCity(World world) {
        this.world = world;
    }

    public void generate() {
        if (world == null) {
            return;
        }

        buildHouse(-15, 20, 9);
        buildHouse(5, 25, 8);
        buildHouse(25, 30, 10);
        buildHouse(-5, 45, 7);

        buildRoad(-25, 15, 35);
    }

    private void buildHouse(int x, int z, int size) {
        int y = 91;

        for (int dx = 0; dx < size; dx++) {
            for (int dz = 0; dz < size; dz++) {
                world.getBlockAt(x + dx, y, z + dz)
                        .setType(Material.CRACKED_STONE_BRICKS, false);
            }
        }

        for (int height = 1; height <= 5; height++) {
            for (int i = 0; i < size; i++) {

                if (random.nextInt(100) < 18 && height > 2) {
                    continue;
                }

                setWall(x + i, y + height, z);
                setWall(x + i, y + height, z + size - 1);
                setWall(x, y + height, z + i);
                setWall(x + size - 1, y + height, z + i);
            }
        }

        for (int dx = 1; dx < size - 1; dx++) {
            for (int dz = 1; dz < size - 1; dz++) {
                if (random.nextInt(100) < 45) {
                    world.getBlockAt(
                            x + dx,
                            y + 6,
                            z + dz
                    ).setType(Material.STONE_BRICK_SLAB, false);
                }
            }
        }

        world.getBlockAt(x + size / 2, y + 1, z)
                .setType(Material.AIR, false);

        world.getBlockAt(x + size / 2, y + 2, z)
                .setType(Material.AIR, false);
    }

    private void setWall(int x, int y, int z) {
        Material[] materials = {
                Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS
        };

        world.getBlockAt(x, y, z)
                .setType(
                        materials[random.nextInt(materials.length)],
                        false
                );
    }

    private void buildRoad(int startX, int z, int length) {
        for (int i = 0; i < length; i++) {
            world.getBlockAt(startX + i, 90, z)
                    .setType(Material.POLISHED_DEEPSLATE, false);
        }
    }
}
