package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DeathRealmResurrection {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;

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
    }

    public void resurrect(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        Location destination = player.getBedSpawnLocation();

        if (destination == null || destination.getWorld() == null) {
            World mainWorld = Bukkit.getWorld("world");

            if (mainWorld == null) {
                plugin.getLogger().severe(
                        "Dunia utama bernama world tidak ditemukan!"
                );
                return;
            }

            destination = mainWorld.getSpawnLocation();
        }

        timerManager.removeTimer(player.getUniqueId());
        bossBar.hide(player);

        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setFallDistance(0);

        player.teleport(destination);

        player.sendMessage(
                "§aKamu telah dibangkitkan kembali!"
        );

        visibility.refreshAll();
    }
}
