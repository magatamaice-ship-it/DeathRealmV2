
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DeathRealmFriendRevivalManager implements Listener {

    private static final int RITUAL_SECONDS = 180;

    private final DeathRealm plugin;
    private final File file;

    private final List<Requirement> requirements =
            new ArrayList<>();

    private final Map<UUID, BukkitTask> activeRituals =
            new HashMap<>();

    private final Set<UUID> pendingRevival =
            new HashSet<>();

    private final Set<UUID> pendingInProgress =
            new HashSet<>();

    private DeathRealmResurrection resurrection;

    public DeathRealmFriendRevivalManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(
                plugin.getDataFolder(),
                "revival-requirements.yml"
        );

        load();
    }

    public void setResurrection(
            DeathRealmResurrection resurrection
    ) {
        this.resurrection = resurrection;
    }

    public boolean addRequirement(Player player, int amount) {
        if (player == null || amount <= 0) {
            return false;
        }

        ItemStack held =
                player.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR) {
            return false;
        }

        ItemStack savedItem = held.clone();
        savedItem.setAmount(1);

        for (Requirement requirement : requirements) {
            if (requirement.getItem().isSimilar(savedItem)) {
                return false;
            }
        }

        requirements.add(
                new Requirement(savedItem, amount)
        );

        save();
        return true;
    }

    public boolean removeRequirement(int index) {
        if (index < 1 || index > requirements.size()) {
            return false;
        }

        requirements.remove(index - 1);
        save();

        return true;
    }

    public List<Requirement> getRequirements() {
        return Collections.unmodifiableList(requirements);
    }

    public boolean hasRequirements(Player player) {
        if (player == null || !player.isOnline()
                || requirements.isEmpty()) {
            return false;
        }

        for (Requirement requirement : requirements) {
            if (countItem(
                    player,
                    requirement.getItem()
            ) < requirement.getAmount()) {
                return false;
            }
        }

        return true;
    }

    public boolean consumeRequirements(Player player) {
        if (!hasRequirements(player)) {
            return false;
        }

        for (Requirement requirement : requirements) {
            removeItems(
                    player,
                    requirement.getItem(),
                    requirement.getAmount()
            );
        }

        player.updateInventory();
        return true;
    }

    public boolean canRevive(UUID targetId) {
        return targetId != null
                && plugin.getTimerManager().hasTimer(targetId);
    }

    public boolean isPendingRevival(UUID uuid) {
        return uuid != null && pendingRevival.contains(uuid);
    }

    public boolean startRitual(
            Player initiator,
            UUID targetId
    ) {
        if (initiator == null || !initiator.isOnline()
                || targetId == null
                || targetId.equals(initiator.getUniqueId())
                || !canRevive(targetId)
                || activeRituals.containsKey(targetId)
                || !hasRequirements(initiator)
                || resurrection == null) {
            return false;
        }

        Location altar =
                plugin.getAltarManager().getSummoningAltar();

        if (altar == null || altar.getWorld() == null) {
            initiator.sendMessage(
                    "§cLokasi Altar Pemanggilan belum diatur."
            );
            return false;
        }

        if (!consumeRequirements(initiator)) {
            return false;
        }

        initiator.sendMessage(
                "§6Ritual kebangkitan dimulai. Waktu: 3 menit."
        );

        final int[] elapsed = {0};

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    if (!canRevive(targetId)) {
                        cancelRitual(targetId);

                        if (initiator.isOnline()) {
                            initiator.sendMessage(
                                    "§cRitual dibatalkan. Target sudah tidak berstatus mati."
                            );
                        }

                        return;
                    }

                    if (altar.getWorld() == null) {
                        cancelRitual(targetId);
                        return;
                    }

                    elapsed[0]++;

                    playRitualEffects(
                            altar,
                            elapsed[0]
                    );

                    if (elapsed[0] % 30 == 0
                            && initiator.isOnline()) {

                        int remaining =
                                RITUAL_SECONDS - elapsed[0];

                        initiator.sendMessage(
                                "§eRitual kebangkitan: §f"
                                        + remaining
                                        + " detik tersisa."
                        );
                    }

                    if (elapsed[0] >= RITUAL_SECONDS) {
                        finishRitual(
                                initiator,
                                targetId
                        );
                    }
                },
                0L,
                20L
        );

        activeRituals.put(targetId, task);

        return true;
    }

    private void finishRitual(
            Player initiator,
            UUID targetId
    ) {
        BukkitTask task = activeRituals.remove(targetId);

        if (task != null) {
            task.cancel();
        }

        if (!canRevive(targetId)) {
            return;
        }

        Player target = Bukkit.getPlayer(targetId);

        if (target != null && target.isOnline()) {

            resurrection.resurrect(
                    target,
                    success -> {

                        if (!success) {
                            if (initiator.isOnline()) {
                                initiator.sendMessage(
                                        "§cKebangkitan target gagal diselesaikan."
                                );
                            }

                            return;
                        }

                        plugin.getSoulFragmentRitual()
                                .cancelRitual(targetId);

                        if (initiator.isOnline()) {
                            initiator.sendMessage(
                                    "§aRitual selesai! Target berhasil dibangkitkan."
                            );
                        }
                    }
            );

            return;
        }

        cleanupOfflineTarget(targetId);

        pendingRevival.add(targetId);
        save();

        if (initiator.isOnline()) {
            initiator.sendMessage(
                    "§aRitual berhasil! Jiwa target akan kembali saat ia masuk server."
            );
        }
    }

    private void cleanupOfflineTarget(UUID targetId) {
        plugin.getSoulFragmentDisplay()
                .removeFragments(targetId);

        plugin.getSoulFragmentManager()
                .resetTrial(targetId);
    }

    public void cancelRitualForTarget(UUID targetId) {
        if (targetId == null) {
            return;
        }

        BukkitTask task = activeRituals.remove(targetId);

        if (task != null) {
            task.cancel();
        }
    }

    private void cancelRitual(UUID targetId) {
        cancelRitualForTarget(targetId);
    }

    private void playRitualEffects(
            Location altar,
            int elapsed
    ) {
        int stage = elapsed / 45;

        Location center =
                altar.clone().add(0.5, 1.2, 0.5);

        altar.getWorld().spawnParticle(
                Particle.SOUL,
                center,
                4 + stage * 3,
                0.6,
                0.5,
                0.6,
                0.03
        );

        if (elapsed % 3 == 0) {
            altar.getWorld().spawnParticle(
                    Particle.END_ROD,
                    center,
                    2 + stage * 2,
                    0.8,
                    0.8,
                    0.8,
                    0.02
            );
        }

        if (elapsed % 10 == 0) {
            altar.getWorld().playSound(
                    center,
                    Sound.BLOCK_BEACON_AMBIENT,
                    0.5f + stage * 0.2f,
                    0.7f + stage * 0.15f
            );
        }

        if (elapsed == 150 || elapsed == 180) {
            altar.getWorld().spawnParticle(
                    Particle.PORTAL,
                    center,
                    80,
                    1.5,
                    1.5,
                    1.5,
                    0.5
            );

            altar.getWorld().playSound(
                    center,
                    Sound.BLOCK_END_PORTAL_SPAWN,
                    1.0f,
                    1.0f
            );
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTargetJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!pendingRevival.contains(uuid)
                || !pendingInProgress.add(uuid)) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {

            if (!player.isOnline()
                    || !pendingRevival.contains(uuid)
                    || resurrection == null) {

                pendingInProgress.remove(uuid);
                return;
            }

            resurrection.resurrect(
                    player,
                    success -> {

                        pendingInProgress.remove(uuid);

                        if (!success) {
                            if (player.isOnline()) {
                                player.sendMessage(
                                        "§cKebangkitan tertunda belum berhasil. Silakan coba masuk kembali."
                                );
                            }

                            return;
                        }

                        pendingRevival.remove(uuid);
                        save();

                        if (player.isOnline()) {
                            player.sendMessage(
                                    "§aRitual temanmu berhasil. Kamu telah kembali ke dunia kehidupan!"
                            );
                        }
                    }
            );
        });
    }

    private int countItem(
            Player player,
            ItemStack template
    ) {
        int total = 0;

        for (ItemStack item :
                player.getInventory().getStorageContents()) {

            if (item != null && item.isSimilar(template)) {
                total += item.getAmount();
            }
        }

        return total;
    }

    private void removeItems(
            Player player,
            ItemStack template,
            int amount
    ) {
        int remaining = amount;

        for (int slot = 0;
             slot < player.getInventory()
                     .getStorageContents().length
                     && remaining > 0;
             slot++) {

            ItemStack item =
                    player.getInventory().getItem(slot);

            if (item == null || !item.isSimilar(template)) {
                continue;
            }

            int taken = Math.min(
                    remaining,
                    item.getAmount()
            );

            item.setAmount(item.getAmount() - taken);
            remaining -= taken;

            if (item.getAmount() <= 0) {
                player.getInventory().setItem(slot, null);
            }
        }
    }

    private void save() {
        if (!plugin.getDataFolder().exists()
                && !plugin.getDataFolder().mkdirs()) {

            plugin.getLogger().severe(
                    "Gagal membuat folder data DeathRealm!"
            );

            return;
        }

        YamlConfiguration config =
                new YamlConfiguration();

        for (int i = 0; i < requirements.size(); i++) {
            Requirement requirement = requirements.get(i);

            config.set(
                    "requirements." + i + ".item",
                    requirement.getItem()
            );

            config.set(
                    "requirements." + i + ".amount",
                    requirement.getAmount()
            );
        }

        for (UUID uuid : pendingRevival) {
            config.set("pending." + uuid, true);
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan persyaratan ritual: "
                            + exception.getMessage()
            );
        }
    }

    private void load() {
        if (!file.exists()) {
            return;
        }

        YamlConfiguration config =
                YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section =
                config.getConfigurationSection("requirements");

        if (section != null) {
            for (String key : section.getKeys(false)) {

                ItemStack item = config.getItemStack(
                        "requirements." + key + ".item"
                );

                int amount = config.getInt(
                        "requirements." + key + ".amount"
                );

                if (item == null
                        || item.getType() == Material.AIR
                        || amount <= 0) {
                    continue;
                }

                item.setAmount(1);

                requirements.add(
                        new Requirement(item, amount)
                );
            }
        }

        ConfigurationSection pending =
                config.getConfigurationSection("pending");

        if (pending != null) {
            for (String key : pending.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);

                    if (pending.getBoolean(key)) {
                        pendingRevival.add(uuid);
                    }

                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning(
                            "UUID pending revival tidak valid: "
                                    + key
                    );
                }
            }
        }

        plugin.getLogger().info(
                "Berhasil memuat "
                        + requirements.size()
                        + " persyaratan dan "
                        + pendingRevival.size()
                        + " kebangkitan tertunda."
        );
    }

    public void stop() {
        for (BukkitTask task : activeRituals.values()) {
            if (task != null) {
                task.cancel();
            }
        }

        activeRituals.clear();
        pendingInProgress.clear();

        save();

        plugin.getLogger().info(
                "Friend Revival Manager berhasil dihentikan."
        );
    }

    public static class Requirement {

        private final ItemStack item;
        private final int amount;

        public Requirement(ItemStack item, int amount) {
            this.item = item.clone();
            this.item.setAmount(1);
            this.amount = amount;
        }

        public ItemStack getItem() {
            return item.clone();
        }

        public int getAmount() {
            return amount;
        }
    }
}
