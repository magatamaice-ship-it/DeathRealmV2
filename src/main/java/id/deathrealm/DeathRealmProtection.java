package id.deathrealm;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class DeathRealmProtection implements Listener {

    private final DeathRealm plugin;

    public DeathRealmProtection(DeathRealm plugin) {
        this.plugin = plugin;
    }

    private boolean isInDeathRealm(Player player) {
        return player.getWorld().getName().equalsIgnoreCase("DeathRealm");
    }

    public void preparePlayer(Player player) {
        if (!isInDeathRealm(player)) {
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setHealth(player.getMaxHealth());
        player.setFallDistance(0);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player
                && isInDeathRealm(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player
                && isInDeathRealm(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInDeathRealm(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInDeathRealm(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isInDeathRealm(event.getPlayer())) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getGameMode() != GameMode.ADVENTURE) {
            player.setGameMode(GameMode.ADVENTURE);
        }

        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
        }

        if (!player.isFlying()) {
            player.setFlying(true);
        }
    }
}
