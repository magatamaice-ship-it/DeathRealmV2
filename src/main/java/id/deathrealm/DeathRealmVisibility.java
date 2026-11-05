package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DeathRealmVisibility implements Listener {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;

    public DeathRealmVisibility(
            DeathRealm plugin,
            DeathTimerManager timerManager
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
    }

    private boolean isDead(Player player) {
        return timerManager.hasTimer(player.getUniqueId());
    }

    private boolean isInDeathRealm(Player player) {
        return player.getWorld().getName()
                .equalsIgnoreCase("DeathRealm");
    }

    public void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            updateVisibility(viewer);
        }
    }

    public void updateVisibility(Player viewer) {
        boolean viewerDead = isDead(viewer)
                && isInDeathRealm(viewer);

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }

            boolean targetDead = isDead(target)
                    && isInDeathRealm(target);

            if (viewerDead == targetDead) {
                viewer.showPlayer(plugin, target);
            } else {
                viewer.hidePlayer(plugin, target);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(
                plugin,
                this::refreshAll
        );
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().runTask(
                plugin,
                this::refreshAll
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Bukkit.getScheduler().runTask(
                plugin,
                this::refreshAll
        );
    }
}
