
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

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

    // Kompatibilitas dengan pemanggilan sistem lama.
    public boolean resurrect(Player player) {
        return resurrect(player, null);
    }

    // Callback hanya menerima true setelah teleport berhasil.
    public boolean resurrect(
            Player player,
            Consumer<Boolean> onComplete
    ) {
        if (player == null || !player.isOnline()) {
            notifyResult(onComplete, false);
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (!timerManager.hasTimer(uuid)
                || !resurrecting.add(uuid)) {
            notifyResult(onComplete, false);
            return false;
        }

        Location destination = findDestination(player);

        if (destination == null) {
            plugin.getLogger().severe(
                    "Lokasi kebangkitan tidak tersedia untuk "
                    + player.getName()
            );

            resurrecting.remove(uuid);
            notifyResult(onComplete, false);
            return false;
        }

        Location finalDestination = destination.clone();

        player.sendMessage(
                "§eJiwamu sedang kembali ke dunia kehidupan..."
        );

        cinematic.play(player, () -> {

            if (!player.isOnline()) {
                resurrecting.remove(uuid);
                notifyResult(onComplete, false);
                return;
            }

            if (!timerManager.hasTimer(uuid)) {
                resurrecting.remove(uuid);
                notifyResult(onComplete, false);
                return;
            }

            if (finalDestination.getWorld() == null) {
                plugin.getLogger().severe(
                        "Dunia tujuan kebangkitan tidak tersedia untuk "
                        + player.getName()
                );

                resurrecting.remove(uuid);
                notifyResult(onComplete, false);
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

                notifyResult(onComplete, false);
                return;
            }

            // Bersihkan tampilan dan data fragmen.
            plugin.getSoulFragmentDisplay().removeFragments(uuid);
            plugin.getSoulFragmentManager().resetTrial(uuid);

            // Hapus timer kematian.
            timerManager.removeTimer(uuid);

            // Sembunyikan BossBar.
            bossBar.hide(player);

            // Pulihkan kondisi pemain.
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

            // Kebangkitan benar-benar berhasil.
            notifyResult(onComplete, true);
        });

        return true;
    }

    private void notifyResult(
            Consumer<Boolean> callback,
            boolean success
    ) {
        if (callback != null) {
            callback.accept(success);
        }
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
