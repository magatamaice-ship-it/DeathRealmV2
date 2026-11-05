package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.World;

public class DeathRealmTemple {

    private final World world;

    public DeathRealmTemple(World world) {
        this.world = world;
    }

    public void generate() {
        if (world == null) {
            return;
        }

        int x = 0;
        int y = 91;
        int z = -45;

        buildFloor(x, y, z);
        buildPillars(x, y, z);
        buildAltar(x, y, z);
    }

    private void buildFloor(int x, int y, int z) {
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 10) {
                    world.getBlockAt(x + dx, y, z + dz)
                            .setType(Material.POLISHED_DEEPSLATE, false);
                }
            }
        }
    }

    private void buildPillars(int x, int y, int z) {
        int[][] positions = {
                {-6, -6}, {-6, 6}, {6, -6}, {6, 6}
        };

        for (int[] position : positions) {
            for (int height = 1; height <= 7; height++) {
                world.getBlockAt(
                        x + position[0],
                        y + height,
                        z + position[1]
                ).setType(Material.DEEPSLATE_BRICKS, false);
            }
        }
    }

    private void buildAltar(int x, int y, int z) {
        world.getBlockAt(x, y + 1, z)
                .setType(Material.CHISELED_DEEPSLATE, false);

        world.getBlockAt(x, y + 2, z)
                .setType(Material.RESPAWN_ANCHOR, false);

        world.getBlockAt(x, y + 3, z)
                .setType(Material.SOUL_LANTERN, false);

        world.getBlockAt(x + 1, y + 1, z)
                .setType(Material.SOUL_TORCH, false);

        world.getBlockAt(x - 1, y + 1, z)
                .setType(Material.SOUL_TORCH, false);

        world.getBlockAt(x, y + 1, z + 1)
                .setType(Material.SOUL_TORCH, false);

        world.getBlockAt(x, y + 1, z - 1)
                .setType(Material.SOUL_TORCH, false);
    }
}
