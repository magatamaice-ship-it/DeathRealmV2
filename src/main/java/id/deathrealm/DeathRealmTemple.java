package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class DeathRealmTemple {

    private final World world;

    private static final int CENTER_X = 0;
    private static final int CENTER_Y = 91;
    private static final int CENTER_Z = -45;

    public DeathRealmTemple(World world) {
        this.world = world;
    }

    public void generate() {
        if (world == null) {
            return;
        }

        buildFloor();
        buildPillars();
        buildAltar();
        buildMysteriousLights();
    }

    private void buildFloor() {
        for (int x = -7; x <= 7; x++) {
            for (int z = -7; z <= 7; z++) {
                setBlock(
                        CENTER_X + x,
                        CENTER_Y,
                        CENTER_Z + z,
                        Material.POLISHED_DEEPSLATE
                );
            }
        }
    }

    private void buildPillars() {
        int[][] positions = {
                {-6, -6},
                {6, -6},
                {-6, 6},
                {6, 6}
        };

        for (int[] position : positions) {
            for (int y = 1; y <= 7; y++) {
                setBlock(
                        CENTER_X + position[0],
                        CENTER_Y + y,
                        CENTER_Z + position[1],
                        Material.DEEPSLATE_BRICKS
                );
            }

            setBlock(
                    CENTER_X + position[0],
                    CENTER_Y + 8,
                    CENTER_Z + position[1],
                    Material.SOUL_LANTERN
            );
        }
    }

    private void buildAltar() {
        setBlock(
                CENTER_X,
                CENTER_Y + 1,
                CENTER_Z,
                Material.CHISELED_DEEPSLATE
        );

        setBlock(
                CENTER_X,
                CENTER_Y + 2,
                CENTER_Z,
                Material.RESPAWN_ANCHOR
        );

        setBlock(
                CENTER_X,
                CENTER_Y + 3,
                CENTER_Z,
                Material.SOUL_LANTERN
        );

        int[][] positions = {
                {2, 0},
                {-2, 0},
                {0, 2},
                {0, -2}
        };

        for (int[] position : positions) {
            setBlock(
                    CENTER_X + position[0],
                    CENTER_Y + 1,
                    CENTER_Z + position[1],
                    Material.SOUL_TORCH
            );
        }
    }

    private void buildMysteriousLights() {
        int[][] positions = {
                {3, 3},
                {-3, 3},
                {3, -3},
                {-3, -3}
        };

        for (int[] position : positions) {
            int x = CENTER_X + position[0];
            int z = CENTER_Z + position[1];

            setBlock(
                    x,
                    CENTER_Y + 1,
                    z,
                    Material.CRYING_OBSIDIAN
            );

            setBlock(
                    x,
                    CENTER_Y + 2,
                    z,
                    Material.SOUL_LANTERN
            );

            setBlock(
                    x,
                    CENTER_Y + 3,
                    z,
                    Material.AIR
            );
        }

        for (int y = 1; y <= 3; y++) {
            setBlock(
                    CENTER_X,
                    CENTER_Y + y,
                    CENTER_Z + 4,
                    Material.PURPLE_STAINED_GLASS
            );

            setBlock(
                    CENTER_X,
                    CENTER_Y + y,
                    CENTER_Z - 4,
                    Material.PURPLE_STAINED_GLASS
            );
        }
    }

    private void setBlock(
            int x,
            int y,
            int z,
            Material material
    ) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(material, false);
    }
}
