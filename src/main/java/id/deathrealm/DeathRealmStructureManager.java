package id.deathrealm;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DeathRealmStructureManager {

    private final DeathRealm plugin;
    private final File file;

    public DeathRealmStructureManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(
                plugin.getDataFolder(),
                "structures.yml"
        );
    }

    public boolean isGenerated() {
        if (!file.exists()) {
            return false;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        return config.getBoolean("generated", false);
    }

    public void generate(World world) {
        if (world == null || isGenerated()) {
            return;
        }

        new DeathRealmIsland(plugin, world).generate(() -> {
            new DeathRealmDeadForest(world).generate();
            new DeathRealmRuins(world).generate();
            new DeathRealmTemple(world).generate();
            new DeathRealmCastle(world).generate();

            saveGenerated();

            plugin.getLogger().info(
                    "Seluruh struktur DeathRealm berhasil dibuat dan disimpan!"
            );
        });
    }

    private void saveGenerated() {
        YamlConfiguration config = new YamlConfiguration();

        config.set("generated", true);

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan status struktur DeathRealm: "
                    + exception.getMessage()
            );
        }
    }
}
