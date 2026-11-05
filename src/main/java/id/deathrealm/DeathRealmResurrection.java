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

    public boolean resurrect(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (!timerManager.hasTimer(uuid)
                || !resurrecting.add(uuid)) {
            return false;
        }

        Location destination = findDestination(player);

        if (destination == null) {
            plugin.getLogger().severe(
                    "Lokasi kebangkitan tidak tersedia untuk "
                    + player.getName()
            );

            resurrecting.remove(uuid);
            return false;
        }

        Location finalDestination = destination.clone();

        player.sendMessage(
                "§eJiwamu sedang kembali ke dunia kehidupan..."
        );

        cinematic.play(player, () -> {

            if (!player.isOnline()) {
                resurrecting.remove(uuid);
                return;
            }

            if (finalDestination.getWorld() == null) {
                resurrecting.remove(uuid);
                return;
            }

            boolean teleported = player.teleport(finalDestination);

            if (!teleported) {
                plugin.getLogger().severe(
                        "Gagal memindahkan " + player.getName()
                        + ". Timer tetap aktif."
                );

                resurrecting.remove(uuid);

                player.sendMessage(
                        "§cKebangkitan gagal. Jiwamu masih berada di DeathRealm."
                );

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

            plugin.getMessages().sendResurrectionMessage(player);

            resurrecting.remove(uuid);
            visibility.refreshAll();
        });

        return true;
    }

    private Location findDestination(Player player) {
        Location bed = player.getBedSpawnLocation();

        if (isValidDestination(bed)) {
            return bed;
        }

        World mainWorld = Bukkit.getWorld("world");

        if (mainWorld != null) {
            Location spawn = mainWorld.getSpawnLocation();

            if (isValidDestination(spawn)) {
                return spawn;
            }
        }

        return null;
    }

    private boolean isValidDestination(Location location) {
        return location != null
                && location.getWorld() != null
                && Double.isFinite(location.getX())
                && Double.isFinite(location.getY())
                && Double.isFinite(location.getZ());
    }
}
