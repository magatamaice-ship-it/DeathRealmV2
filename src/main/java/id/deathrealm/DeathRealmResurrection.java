package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class DeathRealmResurrection {

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmBossBar bossBar;
    private final DeathRealmVisibility visibility;
    private final DeathRealmResurrectionCinematic cinematic;

    private final Set<UUID> resurrecting = new HashSet<>();

    public DeathRealmResurrection(
            DeathRealm plugin,
            DeathTimerManager timerManager,
            DeathRealmBossBar bossBar,
            DeathRealmVisibility visibility
    ) {
        this.plugin = plugin;
        this.timerManager = timerManager;
        this.bossBar = bossBar;
        this.visibility = visibility;
        this.cinematic = new DeathRealmResurrectionCinematic(plugin);
    }

    public boolean isResurrecting(UUID uuid) {
        return resurrecting.contains(uuid);
    }

    public boolean resurrect(Player player) {
        return resurrect(player, (Consumer<Boolean>) null);
    }

    public boolean resurrect(
            Player player,
            Consumer<Boolean> onComplete
    ) {
        return resurrectInternal(player, null, onComplete);
    }

    // Digunakan untuk kebangkitan melalui ritual teman.
    public boolean resurrect(
            Player player,
            Location destination,
            Consumer<Boolean> onComplete
    ) {
        return resurrectInternal(player, destination, onComplete);
    }

    private boolean resurrectInternal(
            Player player,
            Location requestedDestination,
            Consumer<Boolean> onComplete
    ) {
        if (player == null || !player.isOnline()) {
            notifyResult(onComplete, false);
            return false;
        }

        UUID uuid = player.getUniqueId();

        if (!timerManager.hasTimer(uuid)
                || !resurrecting.add(uuid)) {
            notifyResult(onComplete, false);
            return false;
        }

        Location destination = requestedDestination == null
                ? findDestination()
                : findSafeLocation(requestedDestination);

        if (destination == null) {
            plugin.getLogger().severe(
                    "Lokasi kebangkitan tidak aman untuk "
                            + player.getName()
            );

            resurrecting.remove(uuid);
            notifyResult(onComplete, false);
            return false;
        }

        Location finalDestination = destination.clone();

        player.sendMessage(
                "§eJiwamu sedang kembali ke dunia kehidupan..."
        );

        try {
            cinematic.play(player, () -> {

                if (!player.isOnline()
                        || !timerManager.hasTimer(uuid)
                        || finalDestination.getWorld() == null) {
                    resurrecting.remove(uuid);
                    notifyResult(onComplete, false);
                    return;
                }

                boolean teleported = player.teleport(finalDestination);

                if (!teleported) {
                    resurrecting.remove(uuid);
                    player.sendMessage(
                            "§cKebangkitan gagal. Jiwamu masih berada di DeathRealm."
                    );
                    notifyResult(onComplete, false);
                    return;
                }

                plugin.getSoulFragmentDisplay().removeFragments(uuid);
                plugin.getSoulFragmentManager().resetTrial(uuid);

                timerManager.removeTimer(uuid);
                bossBar.hide(player);

                restoreLivingPlayer(player);

                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()
                            || player.getWorld().getName()
                            .equalsIgnoreCase("DeathRealm")) {
                        return;
                    }

                    restoreLivingPlayer(player);
                    visibility.refreshAll();
                });

                plugin.getMessages().sendResurrectionMessage(player);

                resurrecting.remove(uuid);
                visibility.refreshAll();

                notifyResult(onComplete, true);
            });

        } catch (Exception exception) {
            resurrecting.remove(uuid);

            plugin.getLogger().severe(
                    "Kesalahan cinematic kebangkitan: "
                            + exception.getMessage()
            );

            notifyResult(onComplete, false);
            return false;
        }

        return true;
    }
        private void restoreLivingPlayer(Player player) {
        player.setInvulnerable(false);
        player.setNoDamageTicks(0);

        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGameMode(GameMode.SURVIVAL);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setSaturation(0);
        player.setExhaustion(0);

        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setFreezeTicks(0);
        player.setArrowsInBody(0);
        player.setRemainingAir(player.getMaximumAir());
    }

    private void notifyResult(
            Consumer<Boolean> callback,
            boolean success
    ) {
        if (callback != null) {
            callback.accept(success);
        }
    }

    // Kebangkitan sendiri menggunakan lokasi acak.
    // Tidak menggunakan bed atau world spawn.
    private Location findDestination() {
        World mainWorld = null;

        for (World world : Bukkit.getWorlds()) {
            if (!world.getName().equalsIgnoreCase("DeathRealm")) {
                mainWorld = world;
                break;
            }
        }

        if (mainWorld == null) {
            return null;
        }

        var border = mainWorld.getWorldBorder();

        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();
        double halfSize = border.getSize() / 2.0;

        double margin = Math.min(16.0, halfSize / 4.0);

        double minX = centerX - halfSize + margin;
        double maxX = centerX + halfSize - margin;
        double minZ = centerZ - halfSize + margin;
        double maxZ = centerZ + halfSize - margin;

        if (minX >= maxX || minZ >= maxZ) {
            return null;
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();

        // Mencoba beberapa lokasi sampai menemukan permukaan aman.
        for (int attempt = 0; attempt < 120; attempt++) {
            int x = (int) Math.floor(
                    random.nextDouble(minX, maxX)
            );

            int z = (int) Math.floor(
                    random.nextDouble(minZ, maxZ)
            );

            Block highest = mainWorld.getHighestBlockAt(
                    x,
                    z,
                    HeightMap.MOTION_BLOCKING_NO_LEAVES
            );

            Location candidate = new Location(
                    mainWorld,
                    x + 0.5,
                    highest.getY() + 1,
                    z + 0.5
            );

            Location safe = findSafeLocation(candidate);

            if (safe != null) {
                return safe;
            }
        }

        return null;
    }

    private Location findSafeLocation(Location base) {
        if (base == null || base.getWorld() == null) {
            return null;
        }

        World world = base.getWorld();

        for (int height = 0; height <= 5; height++) {
            Location candidate = base.clone().add(0, height, 0);

            candidate.setX(candidate.getBlockX() + 0.5);
            candidate.setZ(candidate.getBlockZ() + 0.5);

            int y = candidate.getBlockY();

            if (y <= world.getMinHeight()
                    || y + 1 >= world.getMaxHeight()) {
                continue;
            }

            if (!world.getWorldBorder().isInside(candidate)) {
                continue;
            }

            Block feet = candidate.getBlock();
            Block head = candidate.clone().add(0, 1, 0).getBlock();
            Block ground = candidate.clone().subtract(0, 1, 0).getBlock();

            if (!feet.isPassable() || !head.isPassable()) {
                continue;
            }

            Material groundType = ground.getType();

            if (!groundType.isSolid()
                    || groundType == Material.MAGMA_BLOCK
                    || groundType == Material.CACTUS
                    || groundType == Material.CAMPFIRE
                    || groundType == Material.SOUL_CAMPFIRE
                    || groundType == Material.FIRE
                    || groundType == Material.SOUL_FIRE
                    || groundType == Material.LAVA) {
                continue;
            }

            return candidate;
        }

        return null;
    }
}
