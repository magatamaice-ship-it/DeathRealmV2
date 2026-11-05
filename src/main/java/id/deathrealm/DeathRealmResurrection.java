package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathRealmResurrection {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;
    private final DeathRealmResurrectionCinematic cinematic;

    private final Set<UUID> resurrecting = new HashSet<>();

    public DeathRealmResurrection(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmBossBar bossBar,
            DeathRealmVisibility visibility
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.bossBar = bossBar;
        this.visibility = visibility;
        this.cinematic = new DeathRealmResurrectionCinematic(plugin);
    }

    public void resurrect(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (!timerManager.hasTimer(uuid)
                || !resurrecting.add(uuid)) {
            return;
        }

        Location destination = player.getBedSpawnLocation();

        if (destination == null || destination.getWorld() == null) {
            World mainWorld = Bukkit.getWorld("world");

            if (mainWorld == null) {
                plugin.getLogger().severe(
                        "Dunia utama bernama world tidak ditemukan!"
                );
                resurrecting.remove(uuid);
                return;
            }

            destination = mainWorld.getSpawnLocation();
        }

        Location finalDestination = destination.clone();

        player.sendMessage("§eJiwamu sedang kembali ke dunia kehidupan...");

        cinematic.play(player, () -> {
            if (!player.isOnline()) {
                resurrecting.remove(uuid);
                return;
            }

            timerManager.removeTimer(uuid);
            bossBar.hide(player);

            player.setFlying(false);
            player.setAllowFlight(false);
            player.setGameMode(GameMode.SURVIVAL);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setSaturation(20);
            player.setFallDistance(0);

            player.teleport(finalDestination);

            player.sendMessage(
                    "§aKamu telah dibangkitkan kembali!"
            );

            resurrecting.remove(uuid);
            visibility.refreshAll();
        });
    }
}
