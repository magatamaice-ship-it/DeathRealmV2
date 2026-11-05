package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.World;

public class DeathRealmRuins {

    private final World world;

    public DeathRealmRuins(World world) {
        this.world = world;
    }

    public void generate() {
        if (world == null) {
            return;
        }

        buildRuins(-25, -20);
        buildRuins(30, 25);
        buildRuins(-35, 35);
    }

    private void buildRuins(int x, int z) {
        int y = 91;

        Material[] materials = {
                Material.STONE_BRICKS,
                Material.CRACKED_STONE_BRICKS,
                Material.MOSSY_STONE_BRICKS
        };

        for (int dx = 0; dx < 9; dx++) {
            for (int dz = 0; dz < 9; dz++) {
                if (dx == 0 || dx == 8 || dz == 0 || dz == 8) {
                    world.getBlockAt(x + dx, y, z + dz)
                            .setType(
                                    materials[(dx + dz) % materials.length],
                                    false
                            );
                }
            }
        }

        for (int height = 1; height <= 5; height++) {
            world.getBlockAt(x, y + height, z)
                    .setType(Material.STONE_BRICK_WALL, false);

            world.getBlockAt(x + 8, y + height, z)
                    .setType(Material.STONE_BRICK_WALL, false);

            world.getBlockAt(x, y + height, z + 8)
                    .setType(Material.STONE_BRICK_WALL, false);

            world.getBlockAt(x + 8, y + height, z + 8)
                    .setType(Material.STONE_BRICK_WALL, false);
        }

        world.getBlockAt(x + 4, y + 1, z)
                .setType(Material.AIR, false);

        world.getBlockAt(x + 4, y + 2, z)
                .setType(Material.AIR, false);

        world.getBlockAt(x + 4, y + 3, z)
                .setType(Material.AIR, false);

        world.getBlockAt(x + 4, y + 4, z)
                .setType(Material.AIR, false);
    }
}
