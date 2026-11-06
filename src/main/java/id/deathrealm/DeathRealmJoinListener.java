
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class DeathRealmJoinListener implements Listener {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmWorld worldManager;
    private final DeathRealmProtection protection;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;
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
        this.entryEffect = new DeathRealmEntryEffect(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Bukkit.getScheduler().runTask(plugin, () -> {

            if (!player.isOnline()) {
                return;
            }

            // Pemain dengan kebangkitan tertunda harus
            // menyelesaikan proses resurrection terlebih dahulu.
            if (plugin.getFriendRevivalManager()
                    .isPendingRevival(uuid)) {
                return;
            }

            // Pemain yang tidak memiliki timer adalah pemain hidup.
            if (!timerManager.hasTimer(uuid)) {
                visibility.refreshAll();
                return;
            }

            if (worldManager.getWorld() == null) {
                plugin.getLogger().warning(
                        "Dunia DeathRealm belum tersedia saat pemain masuk."
                );
                return;
            }

            Location destination =
                    worldManager.getWorld().getSpawnLocation();

            boolean teleported = player.teleport(destination);

            if (!teleported) {
                plugin.getLogger().warning(
                        "Gagal mengirim pemain ke DeathRealm: "
                                + player.getName()
                );
                return;
            }

            protection.preparePlayer(player);

            // Jika waktu tunggu sudah selesai, buka menu percobaan.
            if (timerManager.isTrialUnlocked(uuid)) {

                player.sendTitle(
                        "§5ALAMKACUNG",
                        "§dPercobaan kebangkitan telah terbuka",
                        10,
                        60,
                        20
                );

                player.sendMessage(
                        "§dWaktumu telah habis. Percobaan kebangkitan sekarang tersedia."
                );

                Bukkit.getScheduler().runTaskLater(
                        plugin,
                        () -> {

                            if (!player.isOnline()
                                    || !timerManager.hasTimer(uuid)
                                    || !timerManager.isTrialUnlocked(uuid)
                                    || plugin.getFriendRevivalManager()
                                    .isPendingRevival(uuid)) {
                                return;
                            }

                            if (plugin.getSoulFragmentManager()
                                    .getActiveTrialOwners()
                                    .contains(uuid)) {

                                plugin.getSoulFragmentDisplay()
                                        .spawnFragments(
                                                uuid,
                                                player.getName()
                                        );

                                player.sendMessage(
                                        "§dPercobaanmu masih berlangsung. Kumpulkan ketiga Fragmen Jiwa."
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

                player.sendMessage(
                        "§5Kamu berada di ALAMKACUNG."
                );
            }

            visibility.refreshAll();
        });
    }
}
