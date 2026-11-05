package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class DeathRealmVerticalBoundary implements Listener {

    private static final int MIN_Y = 50;
    private static final int MAX_Y = 150;

    private final DeathRealm plugin;

    public DeathRealmVerticalBoundary(DeathRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            return;
        }

        Location destination = event.getTo();

        if (destination == null
                || destination.getWorld() == null) {
            return;
        }

        if (destination.getWorld().getName()
                .equalsIgnoreCase("DeathRealm")) {

            if (destination.getY() < MIN_Y
                    || destination.getY() > MAX_Y) {
                event.setCancelled(true);
            }

            return;
        }

        // Izinkan teleportasi plugin untuk proses kebangkitan.
        if (event.getCause()
                != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName()
                .equalsIgnoreCase("DeathRealm")) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        if (to.getY() >= MIN_Y && to.getY() <= MAX_Y) {
            return;
        }

        Location safe = from.clone();

        if (safe.getY() < MIN_Y || safe.getY() > MAX_Y) {
            World world = player.getWorld();
            safe = world.getSpawnLocation();
        }

        safe.setX(safe.getBlockX() + 0.5);
        safe.setZ(safe.getBlockZ() + 0.5);
        safe.setY(Math.max(MIN_Y, Math.min(MAX_Y, safe.getY())));

        event.setTo(safe);
        player.setFallDistance(0);
    }
}
