
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DeathRealmAltarManager {

    private final DeathRealm plugin;
    private final File file;

    private Location summoningAltar;
    private Location rebirthAltar;

    public DeathRealmAltarManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(
                plugin.getDataFolder(),
                "altars.yml"
        );

        load();
    }

    public void setSummoningAltar(Location location) {
        summoningAltar = location.getBlock().getLocation();
        save();
    }

    public void setRebirthAltar(Location location) {
        rebirthAltar = location.getBlock().getLocation();
        save();
    }

    public Location getSummoningAltar() {
        return summoningAltar == null
                ? null
                : summoningAltar.clone();
    }

    public Location getRebirthAltar() {
        return rebirthAltar == null
                ? null
                : rebirthAltar.clone();
    }

    public boolean isSummoningAltar(Location location) {
        return isSameBlock(summoningAltar, location);
    }

    public boolean isRebirthAltar(Location location) {
        return isSameBlock(rebirthAltar, location);
    }

    private boolean isSameBlock(
            Location first,
            Location second
    ) {
        if (first == null || second == null
                || first.getWorld() == null
                || second.getWorld() == null) {
            return false;
        }

        return first.getWorld().equals(second.getWorld())
                && first.getBlockX() == second.getBlockX()
                && first.getBlockY() == second.getBlockY()
                && first.getBlockZ() == second.getBlockZ();
    }

    private void save() {
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().severe(
                    "Gagal membuat folder data DeathRealm!"
            );
            return;
        }

        YamlConfiguration config = new YamlConfiguration();

        saveLocation(config, "summoning", summoningAltar);
        saveLocation(config, "rebirth", rebirthAltar);

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan altars.yml: "
                    + exception.getMessage()
            );
        }
    }

    private void saveLocation(
            YamlConfiguration config,
            String path,
            Location location
    ) {
        if (location == null || location.getWorld() == null) {
            return;
        }

        config.set(path + ".world", location.getWorld().getName());
        config.set(path + ".x", location.getBlockX());
        config.set(path + ".y", location.getBlockY());
        config.set(path + ".z", location.getBlockZ());
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        summoningAltar = loadLocation(config, "summoning");
        rebirthAltar = loadLocation(config, "rebirth");
    }

    private Location loadLocation(
            YamlConfiguration config,
            String path
    ) {
        String worldName = config.getString(path + ".world");

        if (worldName == null) {
            return null;
        }

        org.bukkit.World world =
                plugin.getServer().getWorld(worldName);

        if (world == null) {
            plugin.getLogger().warning(
                    "Dunia altar tidak ditemukan: " + worldName
            );
            return null;
        }

        return new Location(
                world,
                config.getDouble(path + ".x"),
                config.getDouble(path + ".y"),
                config.getDouble(path + ".z")
        );
    }
}
