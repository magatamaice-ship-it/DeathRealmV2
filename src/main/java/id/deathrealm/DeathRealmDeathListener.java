package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathRealmDeathListener implements Listener {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmWorld worldManager;
    private final DeathRealmProtection protection;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;
    private final DeathRealmCinematic cinematic;
    private final DeathRealmEntryEffect entryEffect;

    private final Set<UUID> pendingPlayers = new HashSet<>();

    public DeathRealmDeathListener(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmWorld worldManager,
            DeathRealmProtection protection,
            DeathRealmBossBar bossBar,
            DeathRealmVisibility visibility,
            DeathRealmCinematic cinematic
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.worldManager = worldManager;
        this.protection = protection;
        this.bossBar = bossBar;
        this.visibility = visibility;
        this.cinematic = cinematic;
        this.entryEffect = new DeathRealmEntryEffect(plugin);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        timerManager.startTimer(player.getUniqueId());
        pendingPlayers.add(player.getUniqueId());

        visibility.refreshAll();

        cinematic.playDeathAnimation(player, () -> {
            if (!player.isOnline()) {
                return;
            }

            player.spigot().respawn();
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (!pendingPlayers.remove(player.getUniqueId())
                && !timerManager.hasTimer(player.getUniqueId())) {
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
            entryEffect.play(player);
            plugin.getMessages().sendEntryMessage(player);
            visibility.refreshAll();
        });
    }
}
