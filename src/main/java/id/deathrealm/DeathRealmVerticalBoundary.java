package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class DeathRealmVerticalBoundary implements Listener {

    private static final int MIN_Y = 50;
    private static final int MAX_Y = 150;

    private final DeathRealm plugin;

    public DeathRealmVerticalBoundary(DeathRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!player.getWorld().getName().equalsIgnoreCase("DeathRealm")) {
            return;
        }

        Location destination = event.getTo();

        if (destination == null) {
            return;
        }

        if (destination.getY() >= MIN_Y
                && destination.getY() <= MAX_Y) {
            return;
        }

        Location safeLocation = event.getFrom().clone();

        if (safeLocation.getY() < MIN_Y
                || safeLocation.getY() > MAX_Y) {
            World world = player.getWorld();
            safeLocation = world.getSpawnLocation();
        }

        safeLocation.setX(safeLocation.getBlockX() + 0.5);
        safeLocation.setZ(safeLocation.getBlockZ() + 0.5);

        player.teleport(safeLocation);
        player.setFallDistance(0);
    }
}
