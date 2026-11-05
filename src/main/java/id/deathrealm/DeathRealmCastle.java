package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class DeathRealmCastle {

    private final World world;
    private final Random random = new Random(4521);

    public DeathRealmCastle(World world) {
        this.world = world;
    }

    public void generate() {
        if (world == null) {
            return;
        }

        int x = 30;
        int y = 91;
        int z = -25;

        buildFloor(x, y, z);
        buildWalls(x, y, z);
        buildTowers(x, y, z);
        buildGate(x, y, z);
    }

    private void buildFloor(int x, int y, int z) {
        for (int dx = 0; dx < 25; dx++) {
            for (int dz = 0; dz < 25; dz++) {
                world.getBlockAt(x + dx, y, z + dz)
                        .setType(Material.STONE_BRICKS, false);
            }
        }
    }

    private void buildWalls(int x, int y, int z) {
        for (int height = 1; height <= 7; height++) {
            for (int i = 0; i < 25; i++) {
                if (random.nextInt(100) < 20 && height > 3) {
                    continue;
                }

                world.getBlockAt(x + i, y + height, z)
                        .setType(Material.CRACKED_STONE_BRICKS, false);

                world.getBlockAt(x + i, y + height, z + 24)
                        .setType(Material.STONE_BRICKS, false);

                world.getBlockAt(x, y + height, z + i)
                        .setType(Material.MOSSY_STONE_BRICKS, false);

                world.getBlockAt(x + 24, y + height, z + i)
                        .setType(Material.STONE_BRICKS, false);
            }
        }
    }

    private void buildTowers(int x, int y, int z) {
        buildTower(x, y, z);
        buildTower(x + 24, y, z);
        buildTower(x, y, z + 24);
        buildTower(x + 24, y, z + 24);
    }

    private void buildTower(int x, int y, int z) {
        for (int height = 1; height <= 12; height++) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) == 2 || Math.abs(dz) == 2) {
                        world.getBlockAt(
                                x + dx,
                                y + height,
                                z + dz
                        ).setType(Material.STONE_BRICKS, false);
                    }
                }
            }
        }

        world.getBlockAt(x, y + 13, z)
                .setType(Material.COBBLESTONE_WALL, false);
    }

    private void buildGate(int x, int y, int z) {
        for (int height = 1; height <= 5; height++) {
            world.getBlockAt(x + 12, y + height, z)
                    .setType(Material.AIR, false);

            world.getBlockAt(x + 13, y + height, z)
                    .setType(Material.AIR, false);
        }
    }
}
