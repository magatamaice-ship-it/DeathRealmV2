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
    private DeathRealmCoordinateDisplay coordinateDisplay;
    private DeathRealmCinematic cinematic;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Memulai DeathRealmV2...");

        timerManager = new DeathTimerManager(this);

        worldManager = new DeathRealmWorld(this);
        worldManager.createWorld();

        World deathWorld = worldManager.getWorld();

        if (deathWorld == null) {
            getLogger().severe("Dunia DeathRealm gagal dimuat!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        new DeathRealmIsland(this, deathWorld).generate(() -> {
            new DeathRealmDeadForest(deathWorld).generate();
            new DeathRealmRuins(deathWorld).generate();
            new DeathRealmTemple(deathWorld).generate();
            new DeathRealmCastle(deathWorld).generate();

            getLogger().info(
                    "Seluruh struktur utama DeathRealm selesai dibuat!"
            );
        });

        new DeathRealmBoundary(this, deathWorld).applyBoundary();

        protection = new DeathRealmProtection(this);
        visibility = new DeathRealmVisibility(this, timerManager);

        bossBar = new DeathRealmBossBar(this, timerManager);
        cinematic = new DeathRealmCinematic(this);

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

        coordinateDisplay = new DeathRealmCoordinateDisplay(
                this,
                timerManager
        );

        Bukkit.getPluginManager().registerEvents(protection, this);
        Bukkit.getPluginManager().registerEvents(visibility, this);

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmVerticalBoundary(this),
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmDeathListener(
                        this,
                        timerManager,
                        worldManager,
                        protection,
                        bossBar,
                        visibility,
                        cinematic
                ),
                this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmJoinListener(
                        this,
                        timerManager,
                        worldManager,
                        protection,
                        bossBar,
                        visibility,
                        resurrection
                ),
                this
        );

        bossBar.start();
        timerTask.start();
        coordinateDisplay.start();

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

        if (coordinateDisplay != null) {
            coordinateDisplay.stop();
        }

        instance = null;

        getLogger().info("DeathRealmV2 dinonaktifkan.");
    }

    public static DeathRealm getInstance() {
        return instance;
    }

    public DeathTimerManager getTimerManager() {
        return timerManager;
    }
}
