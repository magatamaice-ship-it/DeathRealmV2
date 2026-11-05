package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;

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
            world.setSpawnLocation(0, 91, 0);
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

        world.setAutoSave(true);
        world.setSpawnLocation(0, 91, 0);

        plugin.getLogger().info(
                "Dunia DeathRealm berhasil dibuat!"
        );
    }

    public World getWorld() {
        return world;
    }
}
