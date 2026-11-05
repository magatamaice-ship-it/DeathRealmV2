package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DeathRealmHorizontalBoundary implements Listener {

    private static final double LIMIT = 109.0;

    public DeathRealmHorizontalBoundary(DeathRealm plugin) {
    }

    private boolean isDeathRealm(World world) {
        return world != null
                && world.getName().equalsIgnoreCase("DeathRealm");
    }

    private boolean outside(Location location) {
        return Math.abs(location.getX()) > LIMIT
                || Math.abs(location.getZ()) > LIMIT;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!isDeathRealm(player.getWorld())) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || !outside(to)) {
            return;
        }

        Location safe = to.clone();

        safe.setX(Math.max(-LIMIT, Math.min(LIMIT, safe.getX())));
        safe.setZ(Math.max(-LIMIT, Math.min(LIMIT, safe.getZ())));

        event.setTo(safe);
        player.setFallDistance(0);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Location destination = event.getTo();

        if (!isDeathRealm(destination.getWorld())) {
            return;
        }

        if (outside(destination)) {
            event.setCancelled(true);
        }
    }
}
