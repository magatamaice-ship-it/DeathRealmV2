
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeathRealmPortalProtection implements Listener {

    private final DeathRealm plugin;
    private final Set<UUID> teleportCooldown = new HashSet<>();

    public DeathRealmPortalProtection(DeathRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        if (plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortalLocation(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        UUID uuid = player.getUniqueId();

        if (plugin.getTimerManager().hasTimer(uuid)
                || teleportCooldown.contains(uuid)) {
            return;
        }

        Location entry = plugin.getPortalManager().getPortalEntry();
        Location exit = plugin.getPortalManager().getPortalExit();

        Location destination = null;

        if (isPortalLocation(event.getTo(), entry)) {
            destination = exit;
        } else if (isPortalLocation(event.getTo(), exit)) {
            destination = entry;
        }

        if (destination == null || destination.getWorld() == null) {
            return;
        }

        teleportCooldown.add(uuid);

        player.teleport(destination.clone());

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> teleportCooldown.remove(uuid),
                40L
        );
    }

    private boolean isPortalLocation(
            Location playerLocation,
            Location portalLocation
    ) {
        if (portalLocation == null
                || playerLocation.getWorld() == null
                || portalLocation.getWorld() == null) {
            return false;
        }

        return playerLocation.getWorld().equals(portalLocation.getWorld())
                && playerLocation.getBlockX() == portalLocation.getBlockX()
                && playerLocation.getBlockY() == portalLocation.getBlockY()
                && playerLocation.getBlockZ() == portalLocation.getBlockZ();
    }
}
