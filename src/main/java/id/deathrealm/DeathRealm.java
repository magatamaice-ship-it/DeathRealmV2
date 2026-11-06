
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
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

    private DeathRealmSoulFragmentManager soulFragmentManager;
    private DeathRealmSoulFragmentMenu soulFragmentMenu;
    private DeathRealmSoulFragmentDisplay soulFragmentDisplay;
    private DeathRealmSoulFragmentListener soulFragmentListener;
    private DeathRealmSoulFragmentRitual soulFragmentRitual;

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

        friendRevivalManager.setResurrection(resurrection);

        soulFragmentManager =
                new DeathRealmSoulFragmentManager(this);

        soulFragmentDisplay =
                new DeathRealmSoulFragmentDisplay(
                        this,
                        soulFragmentManager
                );

        soulFragmentMenu =
                new DeathRealmSoulFragmentMenu(
                        this,
                        soulFragmentManager
                );

        soulFragmentListener =
                new DeathRealmSoulFragmentListener(
                        this,
                        soulFragmentManager,
                        soulFragmentDisplay
                );

        soulFragmentRitual =
                new DeathRealmSoulFragmentRitual(
                        this,
                        resurrection
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
                soulFragmentMenu, this
        );

        Bukkit.getPluginManager().registerEvents(
                soulFragmentListener, this
        );

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

        Bukkit.getScheduler().runTaskLater(this, () -> {
            for (java.util.UUID owner :
                    soulFragmentManager.getActiveTrialOwners()) {

                Player onlinePlayer = Bukkit.getPlayer(owner);

                String ownerName = onlinePlayer != null
                        ? onlinePlayer.getName()
                        : Bukkit.getOfflinePlayer(owner).getName();

                if (ownerName == null) {
                    ownerName = owner.toString();
                }

                soulFragmentDisplay.spawnFragments(
                        owner,
                        ownerName
                );
            }
        }, 20L);

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
        // Hentikan ritual kebangkitan sendiri terlebih dahulu.
        if (soulFragmentRitual != null) {
            soulFragmentRitual.stop();
        }

        // Setelah itu, hentikan ritual kebangkitan teman.
        if (friendRevivalManager != null) {
            friendRevivalManager.stop();
        }

        if (timerTask != null) timerTask.stop();
        if (bossBar != null) bossBar.stop();
        if (coordinateDisplay != null) coordinateDisplay.stop();
        if (atmosphere != null) atmosphere.stop();
        if (warningTask != null) warningTask.stop();

        if (soulFragmentListener != null) {
            soulFragmentListener.stop();
        }

        if (soulFragmentDisplay != null) {
            soulFragmentDisplay.stop();
        }

        if (timerManager != null) {
            timerManager.saveTimers();
        }

        if (soulFragmentManager != null) {
            soulFragmentManager.save();
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

    public DeathRealmSoulFragmentManager getSoulFragmentManager() {
        return soulFragmentManager;
    }

    public DeathRealmSoulFragmentMenu getSoulFragmentMenu() {
        return soulFragmentMenu;
    }

    public DeathRealmSoulFragmentDisplay getSoulFragmentDisplay() {
        return soulFragmentDisplay;
    }

    public DeathRealmSoulFragmentRitual getSoulFragmentRitual() {
        return soulFragmentRitual;
    }
}
