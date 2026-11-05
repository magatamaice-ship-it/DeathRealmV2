package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

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

    public void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            updateVisibility(viewer);
        }
    }

    public void updateVisibility(Player viewer) {
        boolean viewerDead = timerManager.hasTimer(
                viewer.getUniqueId()
        );

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(target)) {
                continue;
            }

            boolean targetDead = timerManager.hasTimer(
                    target.getUniqueId()
            );

            if (viewerDead == targetDead) {
                viewer.showPlayer(plugin, target);
            } else {
                viewer.hidePlayer(plugin, target);
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTask(plugin, this::refreshAll);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Bukkit.getScheduler().runTask(plugin, this::refreshAll);
    }
}
