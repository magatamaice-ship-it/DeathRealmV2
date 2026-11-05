package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class DeathRealm extends JavaPlugin {

    private static DeathRealm instance;

    private DeathTimerManager timerManager;
    private DeathRealmWorld worldManager;
    private DeathRealmProtection protection;
    private DeathRealmBossBar bossBar;
    private DeathRealmVisibility visibility;
    private DeathRealmResurrection resurrection;
    private DeathRealmTimerTask timerTask;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Memulai DeathRealmV2...");

        // Penyimpanan timer
        timerManager = new DeathTimerManager(this);

        // Membuat dunia DeathRealm
        worldManager = new DeathRealmWorld(this);
        worldManager.createWorld();

        World deathWorld = worldManager.getWorld();

        if (deathWorld == null) {
            getLogger().severe("Dunia DeathRealm gagal dimuat!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Membuat pulau dan pembatas
        new DeathRealmIsland(this, deathWorld).generate();
        new DeathRealmBoundary(this, deathWorld).applyBoundary();

        // Sistem pemain
        protection = new DeathRealmProtection(this);
        visibility = new DeathRealmVisibility(this, timerManager);

        // Sistem timer dan BossBar
        bossBar = new DeathRealmBossBar(this, timerManager);
        resurrection = new DeathRealmResurrection(
                this,
                timerManager,
                bossBar,
                visibility
        );

        timerTask = new DeathRealmTimerTask(
                this,
                timerManager,
                resurrection
        );

        // Mendaftarkan listener
        Bukkit.getPluginManager().registerEvents(protection, this);

        Bukkit.getPluginManager().registerEvents(
                visibility,
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmDeathListener(
                        this,
                        timerManager,
                        worldManager,
                        protection,
                        bossBar,
                        visibility
                ),
                this
        );

        // Mengaktifkan sistem
        bossBar.start();
        timerTask.start();

        // Mendaftarkan perintah admin
        if (getCommand("lahirkankembali") != null) {
            getCommand("lahirkankembali").setExecutor(
                    new DeathRealmCommand(this, resurrection)
            );
        }

        visibility.refreshAll();

        getLogger().info("DeathRealmV2 berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        if (timerTask != null) {
            timerTask.stop();
        }

        if (bossBar != null) {
            bossBar.stop();
        }

        getLogger().info("DeathRealmV2 dinonaktifkan.");
        instance = null;
    }

    public static DeathRealm getInstance() {
        return instance;
    }

    public DeathTimerManager getTimerManager() {
        return timerManager;
    }
}
