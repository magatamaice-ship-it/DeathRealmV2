
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;

public class DeathRealmPortalProtection implements Listener {

    private final DeathRealm plugin;

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

        if (plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            return;
        }

        Location destination = null;

        if (isPortalLocation(
                event.getTo(),
                plugin.getPortalManager().getPortalEntry()
        )) {
            destination = plugin.getPortalManager().getPortalExit();
        } else if (isPortalLocation(
                event.getTo(),
                plugin.getPortalManager().getPortalExit()
        )) {
            destination = getMainWorldSpawn();
        }

        if (destination == null) {
            return;
        }

        Location finalDestination = destination.clone();

        player.teleportAsync(finalDestination);
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

    private Location getMainWorldSpawn() {
        Location portalExit = plugin.getPortalManager().getPortalExit();

        if (portalExit == null) {
            return null;
        }

        for (org.bukkit.World world : plugin.getServer().getWorlds()) {
            if (!world.getName().equalsIgnoreCase("DeathRealm")) {
                return world.getSpawnLocation();
            }
        }

        return null;
    }
}
