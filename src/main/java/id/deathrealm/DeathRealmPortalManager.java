
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DeathRealmPortalManager {

    private final DeathRealm plugin;
    private final File file;
    private final YamlConfiguration config;

    public DeathRealmPortalManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "portals.yml");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public void setPortalEntry(Location location) {
        config.set("portal-masuk", location);
        save();
    }

    public void setPortalExit(Location location) {
        config.set("portal-keluar", location);
        save();
    }

    public Location getPortalEntry() {
        return config.getLocation("portal-masuk");
    }

    public Location getPortalExit() {
        return config.getLocation("portal-keluar");
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe(
                "Gagal menyimpan lokasi portal ALAMKACUNG!"
            );
            e.printStackTrace();
        }
    }
}
