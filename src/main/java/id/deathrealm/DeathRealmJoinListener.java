
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
    private final DeathRealmEntryEffect entryEffect;

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
        this.entryEffect = new DeathRealmEntryEffect(plugin);
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

            World deathWorld = worldManager.getWorld();

            if (deathWorld == null) {
                plugin.getLogger().severe(
                        "Dunia DeathRealm tidak tersedia saat pemain masuk!"
                );

                player.sendMessage(
                        "§cDeathRealm sedang tidak tersedia. Hubungi admin."
                );

                return;
            }

            boolean teleported = player.teleport(
                    deathWorld.getSpawnLocation()
            );

            if (!teleported
                    || !player.getWorld().equals(deathWorld)) {

                plugin.getLogger().severe(
                        "Gagal mengembalikan "
                        + player.getName()
                        + " ke DeathRealm saat login."
                );

                return;
            }

            protection.preparePlayer(player);

            if (timerManager.isExpired(player.getUniqueId())) {
                timerManager.unlockTrial(player.getUniqueId());

                player.sendTitle(
                        "§5UJIAN KEBANGKITAN",
                        "§dUjianmu telah terbuka",
                        10,
                        70,
                        20
                );

                player.sendMessage(
                        "§5[ALAMKACUNG] §dUjian kebangkitanmu telah terbuka!"
                );

                Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> {
                            if (!player.isOnline()
                                    || !timerManager.hasTimer(
                                            player.getUniqueId()
                                    )
                                    || !timerManager.isTrialUnlocked(
                                            player.getUniqueId()
                                    )) {
                                return;
                            }

                            if (plugin.getSoulFragmentManager()
                                    .isTrialActive(player.getUniqueId())) {

                                plugin.getSoulFragmentDisplay()
                                        .spawnFragments(
                                                player.getUniqueId(),
                                                player.getName()
                                        );

                                player.sendMessage(
                                        "§dPencarian Fragmen Jiwa dilanjutkan."
                                );
                            } else {
                                plugin.getSoulFragmentMenu()
                                        .openMenu(player);
                            }
                        },
                        40L
                );

            } else {
                bossBar.show(player);
                entryEffect.play(player);
                plugin.getMessages().sendEntryMessage(player);

                DeathRealmWarningTask warningTask =
                        plugin.getWarningTask();

                if (warningTask != null) {
                    warningTask.checkPlayer(player);
                }
            }

            visibility.refreshAll();
        });
    }
}
