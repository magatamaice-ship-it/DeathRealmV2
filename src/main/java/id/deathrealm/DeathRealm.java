
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
    private DeathRealmStructureManager structureManager;
    private DeathRealmAtmosphere atmosphere;
    private DeathRealmMessages messages;
    private DeathRealmWarningTask warningTask;
    private DeathRealmAltarManager altarManager;
    private DeathRealmPortalManager portalManager;
    private DeathRealmFriendRevivalManager friendRevivalManager;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Memulai DeathRealmV2...");

        timerManager = new DeathTimerManager(this);
        friendRevivalManager =
                new DeathRealmFriendRevivalManager(this);

        worldManager = new DeathRealmWorld(this);
        worldManager.createWorld();

        World deathWorld = worldManager.getWorld();

        if (deathWorld == null) {
            getLogger().severe("Dunia DeathRealm gagal dimuat!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        structureManager = new DeathRealmStructureManager(this);
        structureManager.generate(deathWorld);

        altarManager = new DeathRealmAltarManager(this);
        portalManager = new DeathRealmPortalManager(this);

        new DeathRealmBoundary(this, deathWorld).applyBoundary();

        protection = new DeathRealmProtection(this);
        visibility = new DeathRealmVisibility(this, timerManager);

        bossBar = new DeathRealmBossBar(this, timerManager);
        cinematic = new DeathRealmCinematic(this);
        atmosphere = new DeathRealmAtmosphere(this);
        messages = new DeathRealmMessages(this);
        warningTask = new DeathRealmWarningTask(this, timerManager);

        resurrection = new DeathRealmResurrection(
                this,
                timerManager,
                bossBar,
                visibility
        );

        // Hubungkan sistem ritual dengan sistem kebangkitan.
        friendRevivalManager.setResurrection(resurrection);

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
                new DeathRealmVerticalBoundary(this), this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmHorizontalBoundary(this), this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmPortalProtection(this), this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmAltarListener(this, altarManager), this
        );

        Bukkit.getPluginManager().registerEvents(
                new DeathRealmCommandProtection(this), this
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
                ), this
        );

        // Listener untuk kebangkitan pemain yang sebelumnya offline.
        Bukkit.getPluginManager().registerEvents(
                friendRevivalManager, this
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
                ), this
        );

        bossBar.start();
        timerTask.start();
        coordinateDisplay.start();
        atmosphere.start();
        warningTask.start();

        if (getCommand("lahirkankembali") != null) {
            getCommand("lahirkankembali").setExecutor(
                    new DeathRealmCommand(this, resurrection)
            );
        }

        if (getCommand("alamkacung") != null) {
            getCommand("alamkacung").setExecutor(
                    new DeathRealmAltarCommand(
                            altarManager,
                            portalManager,
                            friendRevivalManager
                    )
            );
        }

        visibility.refreshAll();

        getLogger().info("DeathRealmV2 berhasil diaktifkan!");
    }

    @Override
    public void onDisable() {
        if (timerTask != null) timerTask.stop();
        if (bossBar != null) bossBar.stop();
        if (coordinateDisplay != null) coordinateDisplay.stop();
        if (atmosphere != null) atmosphere.stop();
        if (warningTask != null) warningTask.stop();
        if (timerManager != null) timerManager.saveTimers();

        instance = null;

        getLogger().info("DeathRealmV2 dinonaktifkan.");
    }

    public static DeathRealm getInstance() {
        return instance;
    }

    public DeathTimerManager getTimerManager() {
        return timerManager;
    }

    public DeathRealmMessages getMessages() {
        return messages;
    }

    public DeathRealmWarningTask getWarningTask() {
        return warningTask;
    }

    public DeathRealmAltarManager getAltarManager() {
        return altarManager;
    }

    public DeathRealmPortalManager getPortalManager() {
        return portalManager;
    }

    public DeathRealmFriendRevivalManager getFriendRevivalManager() {
        return friendRevivalManager;
    }
}
