package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class DeathRealmDeadForest {

    private final World world;
    private final Random random = new Random(83421);

    public DeathRealmDeadForest(World world) {
        this.world = world;
    }

    public void generate() {
        if (world == null) {
            return;
        }

        for (int i = 0; i < 45; i++) {
            int x = random.nextInt(121) - 60;
            int z = random.nextInt(121) - 60;

            if ((x * x) + (z * z) > 70 * 70) {
                continue;
            }

            buildTree(x, z);
        }
    }

    private void buildTree(int x, int z) {
        int groundY = 90;

        if (world.getBlockAt(x, groundY, z).getType()
                != Material.STONE) {
            return;
        }

        int height = 4 + random.nextInt(4);

        for (int y = 1; y <= height; y++) {
            world.getBlockAt(x, groundY + y, z)
                    .setType(Material.DARK_OAK_LOG, false);
        }

        world.getBlockAt(x + 1, groundY + height, z)
                .setType(Material.DARK_OAK_LOG, false);

        world.getBlockAt(x - 1, groundY + height - 1, z)
                .setType(Material.DARK_OAK_LOG, false);

        world.getBlockAt(x, groundY + height, z + 1)
                .setType(Material.DARK_OAK_LOG, false);

        world.getBlockAt(x, groundY + height - 1, z - 1)
                .setType(Material.DARK_OAK_LOG, false);

        world.getBlockAt(x, groundY + height + 1, z)
                .setType(Material.DARK_OAK_LOG, false);
    }
}
