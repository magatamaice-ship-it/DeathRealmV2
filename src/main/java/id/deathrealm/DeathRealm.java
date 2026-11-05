package id.deathrealm;

import org.bukkit.plugin.java.JavaPlugin;

public final class DeathRealm extends JavaPlugin {

    private static DeathRealm instance;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("DeathRealmV2 berhasil diaktifkan!");
        getLogger().info("Target: Paper 1.21.11");
        getLogger().info("Waktu kematian: 25 menit");
    }

    @Override
    public void onDisable() {
        getLogger().info("DeathRealmV2 berhasil dinonaktifkan.");
    }

    public static DeathRealm getInstance() {
        return instance;
    }
}
