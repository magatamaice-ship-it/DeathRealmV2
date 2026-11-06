
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.WorldBorder;

public class DeathRealmWorld {

    private final DeathRealm plugin;
    private World world;

    public DeathRealmWorld(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void createWorld() {
        World existing = Bukkit.getWorld("DeathRealm");

        if (existing != null) {
            world = existing;
            configureWorld();
            return;
        }

        WorldCreator creator = new WorldCreator("DeathRealm");

        creator.environment(World.Environment.NORMAL);
        creator.type(WorldType.NORMAL);
        creator.generator(new VoidChunkGenerator());
        creator.generateStructures(false);

        world = Bukkit.createWorld(creator);

        if (world == null) {
            plugin.getLogger().severe(
                    "Gagal membuat dunia DeathRealm!"
            );
            return;
        }

        configureWorld();

        plugin.getLogger().info(
                "Dunia DeathRealm berhasil dibuat!"
        );
    }

    private void configureWorld() {
        world.setAutoSave(true);
        world.setSpawnLocation(0, 91, 0);

        WorldBorder border = world.getWorldBorder();

        border.setCenter(0, 0);
        border.setSize(220);

        plugin.getLogger().info(
                "Pengaturan dunia DeathRealm berhasil diterapkan."
        );
    }

    public World getWorld() {
        return world;
    }
}
