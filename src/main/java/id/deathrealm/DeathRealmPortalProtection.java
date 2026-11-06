
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

    private static final double PORTAL_RADIUS = 1.5;
    private static final double EXIT_OFFSET = 2.0;

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

        Location entry = plugin.getPortalManager().getPortalEntry();
        Location exit = plugin.getPortalManager().getPortalExit();

        if (teleportCooldown.contains(uuid)) {
            boolean insideEntry = isInsidePortalArea(event.getTo(), entry);
            boolean insideExit = isInsidePortalArea(event.getTo(), exit);

            if (!insideEntry && !insideExit) {
                teleportCooldown.remove(uuid);
            }

            return;
        }

        if (plugin.getTimerManager().hasTimer(uuid)) {
            return;
        }

        Location destination = null;

        if (isInsidePortalArea(event.getTo(), entry)) {
            destination = exit;
        } else if (isInsidePortalArea(event.getTo(), exit)) {
            destination = entry;
        }

        if (destination == null || destination.getWorld() == null) {
            return;
        }

        Location safeDestination = destination.clone();
        safeDestination.add(0, 0, EXIT_OFFSET);

        teleportCooldown.add(uuid);

        boolean success = player.teleport(safeDestination);

        if (!success) {
            teleportCooldown.remove(uuid);
        }
    }

    private boolean isInsidePortalArea(
            Location playerLocation,
            Location portalLocation
    ) {
        if (portalLocation == null
                || playerLocation.getWorld() == null
                || portalLocation.getWorld() == null) {
            return false;
        }

        if (!playerLocation.getWorld().equals(portalLocation.getWorld())) {
            return false;
        }

        return playerLocation.distanceSquared(portalLocation)
                <= PORTAL_RADIUS * PORTAL_RADIUS;
    }
}
