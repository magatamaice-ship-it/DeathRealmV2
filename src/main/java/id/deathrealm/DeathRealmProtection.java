package id.deathrealm;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.projectiles.ProjectileSource;

public class DeathRealmProtection implements Listener {

    private final DeathRealm plugin;

    public DeathRealmProtection(DeathRealm plugin) {
        this.plugin = plugin;
    }

    private boolean isInDeathRealm(Player player) {
        return player.getWorld().getName()
                .equalsIgnoreCase("DeathRealm");
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
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }

        Player attacker = null;

        if (event.getDamager() instanceof Player player) {
            attacker = player;
        } else if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();

            if (shooter instanceof Player player) {
                attacker = player;
            }
        }

        if (attacker == null) {
            return;
        }

        boolean attackerDead = plugin.getTimerManager()
                .hasTimer(attacker.getUniqueId());

        boolean targetDead = plugin.getTimerManager()
                .hasTimer(target.getUniqueId());

        if (attackerDead != targetDead
                || isInDeathRealm(attacker)
                || isInDeathRealm(target)) {
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
}
