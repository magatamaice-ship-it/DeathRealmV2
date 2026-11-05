package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DeathRealmHorizontalBoundary implements Listener {

    private static final double LIMIT = 109.0;

    public DeathRealmHorizontalBoundary(DeathRealm plugin) {
    }

    private boolean outside(Location location) {
        return Math.abs(location.getX()) > LIMIT
                || Math.abs(location.getZ()) > LIMIT;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName()
                .equalsIgnoreCase("DeathRealm")) {
            return;
        }

        Location to = event.getTo();

        if (to == null || !outside(to)) {
            return;
        }

        Location safe = event.getFrom().clone();

        safe.setX(Math.max(-LIMIT, Math.min(LIMIT, safe.getX())));
        safe.setZ(Math.max(-LIMIT, Math.min(LIMIT, safe.getZ())));

        player.teleport(safe);
        player.setFallDistance(0);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!event.getTo().getWorld().getName()
                .equalsIgnoreCase("DeathRealm")) {
            return;
        }

        if (outside(event.getTo())) {
            event.setCancelled(true);
        }
    }
}
