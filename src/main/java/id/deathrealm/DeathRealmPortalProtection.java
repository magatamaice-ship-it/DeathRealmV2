package id.deathrealm;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class DeathRealmPortalProtection implements Listener {

    private final DeathRealm plugin;

    public DeathRealmPortalProtection(DeathRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            return;
        }

        event.setCancelled(true);
    }
}
