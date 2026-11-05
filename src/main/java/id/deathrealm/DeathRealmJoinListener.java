package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DeathRealmJoinListener implements Listener {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmWorld worldManager;
    private final DeathRealmProtection protection;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;
    private final DeathRealmResurrection resurrection;

    public DeathRealmJoinListener(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmWorld worldManager,
            DeathRealmProtection protection,
            DeathRealmBossBar bossBar,
            DeathRealmVisibility visibility,
            DeathRealmResurrection resurrection
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.worldManager = worldManager;
        this.protection = protection;
        this.bossBar = bossBar;
        this.visibility = visibility;
        this.resurrection = resurrection;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (!timerManager.hasTimer(player.getUniqueId())) {
                visibility.refreshAll();
                return;
            }

            if (timerManager.isExpired(player.getUniqueId())) {
                resurrection.resurrect(player);
                return;
            }

            World deathWorld = worldManager.getWorld();

            if (deathWorld == null) {
                plugin.getLogger().severe(
                        "Dunia DeathRealm tidak tersedia saat pemain masuk!"
                );
                return;
            }

            player.teleport(deathWorld.getSpawnLocation());

            protection.preparePlayer(player);
            bossBar.show(player);
            visibility.refreshAll();

            player.sendMessage(
                    "§5Jiwamu kembali ke DeathRealm."
            );
        });
    }
}
