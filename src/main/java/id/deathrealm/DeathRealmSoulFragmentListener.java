
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DeathRealmSoulFragmentListener implements Listener {

    private final DeathRealm plugin;
    private final DeathRealmSoulFragmentManager fragmentManager;
    private final DeathRealmSoulFragmentDisplay fragmentDisplay;

    private final NamespacedKey ownerKey;
    private final NamespacedKey fragmentKey;

    private final Map<UUID, BukkitTask> collecting =
            new HashMap<>();

    private final Map<UUID, Location> startLocations =
            new HashMap<>();

    public DeathRealmSoulFragmentListener(
            DeathRealm plugin,
            DeathRealmSoulFragmentManager fragmentManager,
            DeathRealmSoulFragmentDisplay fragmentDisplay
    ) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
        this.fragmentDisplay = fragmentDisplay;

        ownerKey = new NamespacedKey(plugin, "soul_owner");
        fragmentKey = new NamespacedKey(plugin, "soul_fragment");
    }

    @EventHandler
    public void onFragmentInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) {
            return;
        }

        String ownerText = interaction.getPersistentDataContainer().get(
                ownerKey,
                PersistentDataType.STRING
        );

        Integer fragment = interaction.getPersistentDataContainer().get(
                fragmentKey,
                PersistentDataType.INTEGER
        );

        if (ownerText == null || fragment == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!ownerText.equals(playerId.toString())) {
            player.sendMessage(
                    "§cFragmen ini bukan milikmu!"
            );
            return;
        }

        if (!plugin.getTimerManager().hasTimer(playerId)
                || !plugin.getTimerManager().isTrialUnlocked(playerId)
                || !fragmentManager.isTrialActive(playerId)) {
            player.sendMessage(
                    "§cKamu tidak sedang menjalani ujian Fragmen Jiwa."
            );
            return;
        }

        if (fragmentManager.hasCollected(playerId, fragment)) {
            player.sendMessage(
                    "§cFragmen ini sudah kamu kumpulkan."
            );
            return;
        }

        if (collecting.containsKey(playerId)) {
            player.sendMessage(
                    "§eKamu sedang mengumpulkan fragmen."
            );
            return;
        }

        startCollection(player, fragment, interaction.getLocation());
    }

    private void startCollection(
            Player player,
            int fragment,
            Location location
    ) {
        UUID uuid = player.getUniqueId();

        startLocations.put(uuid, player.getLocation().clone());

        player.sendTitle(
                "§dMENGUMPULKAN FRAGMEN",
                "§7Jangan bergerak selama 3 detik",
                0,
                65,
                5
        );

        player.playSound(
                player.getLocation(),
                Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                1.0F,
                1.0F
        );

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                new Runnable() {
                    private int ticks = 0;

                    @Override
                    public void run() {
                        if (!player.isOnline()
                                || !plugin.getTimerManager().hasTimer(uuid)
                                || !fragmentManager.isTrialActive(uuid)) {
                            cancelCollection(uuid, true);
                            return;
                        }

                        Location current = player.getLocation();
                        Location start = startLocations.get(uuid);

                        if (start == null
                                || !current.getWorld().equals(start.getWorld())
                                || current.distanceSquared(start) > 0.04) {
                            cancelCollection(uuid, true);
                            return;
                        }

                        location.getWorld().spawnParticle(
                                Particle.END_ROD,
                                location.clone().add(0, 0.5, 0),
                                5,
                                0.25,
                                0.3,
                                0.25,
                                0.02
                        );

                        ticks++;

                        if (ticks >= 60) {
                            BukkitTask currentTask = collecting.remove(uuid);

                            if (currentTask != null) {
                                currentTask.cancel();
                            }

                            startLocations.remove(uuid);

                            boolean success =
                                    fragmentManager.collectFragment(
                                            uuid,
                                            fragment
                                    );

                            if (success) {
                                fragmentDisplay.removeFragment(
                                        uuid,
                                        fragment
                                );

                                player.sendTitle(
                                        "§aFRAGMEN DIDAPATKAN",
                                        "§f"
                                                + fragmentManager.getCollectedCount(uuid)
                                                + "/3 Fragmen Jiwa",
                                        5,
                                        50,
                                        10
                                );

                                player.playSound(
                                        player.getLocation(),
                                        Sound.UI_TOAST_CHALLENGE_COMPLETE,
                                        1.0F,
                                        1.0F
                                );

                                player.sendMessage(
                                        "§5[ALAMKACUNG] §aFragmen Jiwa berhasil dikumpulkan!"
                                );
                            } else {
                                player.sendMessage(
                                        "§cPengambilan fragmen gagal. Coba lagi."
                                );
                            }
                        }
                    }
                },
                1L,
                1L
        );

        collecting.put(uuid, task);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (!collecting.containsKey(uuid)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) {
            return;
        }

        if (from.getBlockX() != to.getBlockX()
                || from.getBlockY() != to.getBlockY()
                || from.getBlockZ() != to.getBlockZ()) {
            cancelCollection(uuid, true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cancelCollection(event.getPlayer().getUniqueId(), false);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        cancelCollection(event.getEntity().getUniqueId(), false);
    }

    private void cancelCollection(UUID uuid, boolean notify) {
        BukkitTask task = collecting.remove(uuid);

        if (task != null) {
            task.cancel();
        }

        startLocations.remove(uuid);

        Player player = Bukkit.getPlayer(uuid);

        if (notify && player != null && player.isOnline()) {
            player.sendMessage(
                    "§cPengambilan Fragmen Jiwa dibatalkan karena kamu bergerak."
            );
        }
    }

    public void stop() {
        for (BukkitTask task : collecting.values()) {
            task.cancel();
        }

        collecting.clear();
        startLocations.clear();
    }
                    }
