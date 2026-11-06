
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathRealmSoulFragmentRitual implements Listener {

    private static final int RITUAL_DURATION = 30;

    private final DeathRealm plugin;
    private final DeathRealmResurrection resurrection;
    private final DeathRealmSoulFragmentManager fragmentManager;

    private final Map<UUID, RitualData> activeRituals =
            new HashMap<>();

    public DeathRealmSoulFragmentRitual(
            DeathRealm plugin,
            DeathRealmResurrection resurrection
    ) {
        this.plugin = plugin;
        this.resurrection = resurrection;
        this.fragmentManager = plugin.getSoulFragmentManager();

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public boolean startRitual(Player player) {
        if (player == null || !player.isOnline()) {
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (!plugin.getTimerManager().hasTimer(uuid)) {
            return false;
        }

        if (!fragmentManager.hasAllFragments(uuid)) {
            player.sendMessage(
                    "§cKamu harus mengumpulkan ketiga Fragmen Jiwa!"
            );
            return false;
        }

        if (activeRituals.containsKey(uuid)) {
            player.sendMessage(
                    "§cRitual kebangkitanmu sedang berlangsung!"
            );
            return false;
        }

        BossBar bossBar = Bukkit.createBossBar(
                "§5Ritual Kebangkitan §7- §f30 detik",
                BarColor.PURPLE,
                BarStyle.SOLID
        );

        bossBar.addPlayer(player);
        bossBar.setProgress(1.0);

        RitualData data = new RitualData(
                player.getLocation().clone(),
                bossBar
        );

        activeRituals.put(uuid, data);

        player.sendTitle(
                "§5RITUAL KEBANGKITAN",
                "§dJangan tinggalkan altar!",
                10,
                50,
                10
        );

        player.sendMessage(
                "§5[ALAMKACUNG] §dRitual kebangkitan dimulai!"
        );

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_BEACON_ACTIVATE,
                1.0f,
                0.7f
        );

        data.task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> updateRitual(uuid),
                0L,
                10L
        );

        return true;
    }

    private void updateRitual(UUID uuid) {
        RitualData data = activeRituals.get(uuid);
        Player player = Bukkit.getPlayer(uuid);

        if (data == null) {
            return;
        }

        if (player == null || !player.isOnline()
                || !plugin.getTimerManager().hasTimer(uuid)) {
            cancelRitual(
                    uuid,
                    "§cRitual dibatalkan."
            );
            return;
        }

        data.elapsedSeconds += 0.5;

        double progress = Math.max(
                0.0,
                1.0 - data.elapsedSeconds / RITUAL_DURATION
        );

        data.bossBar.setProgress(progress);

        int remaining = Math.max(
                0,
                (int) Math.ceil(
                        RITUAL_DURATION - data.elapsedSeconds
                )
        );

        data.bossBar.setTitle(
                "§5Ritual Kebangkitan §7- §f"
                        + remaining + " detik"
        );

        Location location = data.location.clone().add(0, 1, 0);

        player.getWorld().spawnParticle(
                Particle.END_ROD,
                location,
                12,
                0.6,
                0.8,
                0.6,
                0.02
        );

        player.getWorld().spawnParticle(
                Particle.PORTAL,
                location,
                18,
                0.5,
                0.7,
                0.5,
                0.2
        );

        if (remaining > 0 && remaining % 5 == 0) {
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_BEACON_AMBIENT,
                    1.0f,
                    1.0f
            );
        }

        if (data.elapsedSeconds >= RITUAL_DURATION) {
            completeRitual(uuid);
        }
    }

    private void completeRitual(UUID uuid) {
        RitualData data = activeRituals.remove(uuid);

        if (data == null) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);

        cleanup(data);

        if (player == null || !player.isOnline()) {
            return;
        }

        player.playSound(
                player.getLocation(),
                Sound.UI_TOAST_CHALLENGE_COMPLETE,
                1.0f,
                1.0f
        );

        player.getWorld().spawnParticle(
                Particle.TOTEM_OF_UNDYING,
                player.getLocation().add(0, 1, 0),
                80,
                1.0,
                1.0,
                1.0,
                0.2
        );

        player.sendTitle(
                "§aKEBANGKITAN BERHASIL",
                "§fJiwamu kembali ke dunia kehidupan",
                10,
                60,
                20
        );

        boolean success = resurrection.resurrect(player);

        if (!success) {
            player.sendMessage(
                    "§cKebangkitan gagal. Kamu masih berada di ALAMKACUNG."
            );
            return;
        }

        // Ritual sendiri berhasil lebih dahulu.
        // Batalkan ritual kebangkitan teman untuk pemain ini.
        plugin.getFriendRevivalManager()
                .cancelRitualForTarget(uuid);
    }

    public void cancelRitual(UUID uuid) {
        cancelRitual(
                uuid,
                "§cRitual kebangkitanmu dibatalkan karena ada kebangkitan lain."
        );
    }

    public void cancelRitual(UUID uuid, String message) {
        RitualData data = activeRituals.remove(uuid);

        if (data == null) {
            return;
        }

        cleanup(data);

        Player player = Bukkit.getPlayer(uuid);

        if (player != null && player.isOnline()) {
            player.sendMessage(message);
            player.playSound(
                    player.getLocation(),
                    Sound.BLOCK_BEACON_DEACTIVATE,
                    1.0f,
                    0.7f
            );
        }
    }

    public boolean isRitualActive(UUID uuid) {
        return activeRituals.containsKey(uuid);
    }

    private void cleanup(RitualData data) {
        if (data.task != null) {
            data.task.cancel();
        }

        data.bossBar.removeAll();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (!activeRituals.containsKey(uuid)
                || event.getTo() == null) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cancelRitual(
                event.getPlayer().getUniqueId(),
                "§cRitual dibatalkan karena kamu keluar dari server."
        );
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        cancelRitual(
                event.getEntity().getUniqueId(),
                "§cRitual dibatalkan karena kamu mati."
        );
    }

    public void stop() {
        for (UUID uuid : activeRituals.keySet().toArray(new UUID[0])) {
            cancelRitual(
                    uuid,
                    "§cRitual dihentikan karena server dimatikan."
            );
        }
    }

    private static class RitualData {

        private final Location location;
        private final BossBar bossBar;
        private BukkitTask task;
        private double elapsedSeconds;

        private RitualData(Location location, BossBar bossBar) {
            this.location = location;
            this.bossBar = bossBar;
        }
    }
}
