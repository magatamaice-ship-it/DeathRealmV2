package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        if (pendingPlayers.contains(uuid)) {
            return;
        }

        timerManager.startTimer(uuid);
        pendingPlayers.add(uuid);

        visibility.refreshAll();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                player.spigot().respawn();
            }
        });
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingPlayers.remove(uuid)
                && !timerManager.hasTimer(uuid)) {
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

            if (!player.getWorld().equals(deathWorld)) {
                boolean teleported = player.teleport(destination);

                if (!teleported) {
                    plugin.getLogger().severe(
                            "Gagal memindahkan "
                            + player.getName()
                            + " ke DeathRealm setelah respawn."
                    );
                    return;
                }
            }

            cinematic.playDeathAnimation(player, () -> {
                if (!player.isOnline()) {
                    return;
                }

                protection.preparePlayer(player);
                bossBar.show(player);
                entryEffect.play(player);

                plugin.getMessages().sendEntryMessage(player);

                visibility.refreshAll();
            });
        });
    }
}
