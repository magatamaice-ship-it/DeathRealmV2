package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DeathRealmDeathListener implements Listener {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmWorld worldManager;
    private final DeathRealmProtection protection;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;

    public DeathRealmDeathListener(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmWorld worldManager,
            DeathRealmProtection protection,
            DeathRealmBossBar bossBar,
            DeathRealmVisibility visibility
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.worldManager = worldManager;
        this.protection = protection;
        this.bossBar = bossBar;
        this.visibility = visibility;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Memulai timer 25 menit.
        timerManager.startTimer(player.getUniqueId());

        // Memperbarui pemisahan pemain hidup dan mati.
        Bukkit.getScheduler().runTask(
                plugin,
                visibility::refreshAll
        );
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!timerManager.hasTimer(player.getUniqueId())) {
            return;
        }

        World deathWorld = worldManager.getWorld();

        if (deathWorld == null) {
            plugin.getLogger().severe(
                    "Dunia DeathRealm tidak tersedia saat respawn!"
            );
            return;
        }

        Location destination = deathWorld.getSpawnLocation();

        event.setRespawnLocation(destination);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            protection.preparePlayer(player);
            bossBar.show(player);
            visibility.refreshAll();
        });
    }
}
