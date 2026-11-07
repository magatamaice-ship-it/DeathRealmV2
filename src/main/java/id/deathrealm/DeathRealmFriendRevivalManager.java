package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
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
import java.util.*;

public class DeathRealmFriendRevivalManager implements Listener {

    private static final int RITUAL_SECONDS = 180;

    private final DeathRealm plugin;
    private final File file;
    private final List<Requirement> requirements = new ArrayList<>();
    private final Map<UUID, BukkitTask> activeRituals = new HashMap<>();
    private final Set<UUID> pendingRevival = new HashSet<>();
    private final Set<UUID> pendingInProgress = new HashSet<>();
    private final Set<UUID> finishingRevival = new HashSet<>();
    private final Map<UUID, RefundData> pendingRefunds = new HashMap<>();

    private DeathRealmResurrection resurrection;

    public DeathRealmFriendRevivalManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "revival-requirements.yml");
        load();
    }

    public void setResurrection(DeathRealmResurrection resurrection) {
        this.resurrection = resurrection;
    }

    public boolean addRequirement(Player player, int amount) {
        if (player == null || amount <= 0) return false;

        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.getType() == Material.AIR) return false;

        ItemStack saved = held.clone();
        saved.setAmount(1);

        for (Requirement requirement : requirements) {
            if (requirement.getItem().isSimilar(saved)) return false;
        }

        requirements.add(new Requirement(saved, amount));
        save();
        return true;
    }

    public boolean removeRequirement(int index) {
        if (index < 1 || index > requirements.size()) return false;
        requirements.remove(index - 1);
        save();
        return true;
    }

    public List<Requirement> getRequirements() {
        return Collections.unmodifiableList(requirements);
    }

    public boolean hasRequirements(Player player) {
        if (player == null || !player.isOnline() || requirements.isEmpty()) {
            return false;
        }

        for (Requirement requirement : requirements) {
            if (countItem(player, requirement.getItem()) < requirement.getAmount()) {
                return false;
            }
        }
        return true;
    }

    public boolean consumeRequirements(Player player) {
        if (!hasRequirements(player)) return false;

        for (Requirement requirement : requirements) {
            removeItems(player, requirement.getItem(), requirement.getAmount());
        }

        player.updateInventory();
        return true;
    }

    public boolean canRevive(UUID targetId) {
        return targetId != null && plugin.getTimerManager().hasTimer(targetId);
    }

    public boolean isPendingRevival(UUID uuid) {
        return uuid != null && pendingRevival.contains(uuid);
    }

    public boolean isRitualActive(UUID uuid) {
        return uuid != null && (activeRituals.containsKey(uuid)
                || finishingRevival.contains(uuid));
    }

    public boolean isFinishingRevival(UUID uuid) {
        return uuid != null && finishingRevival.contains(uuid);
    }

    public boolean startRitual(Player initiator, UUID targetId) {
        if (initiator == null || !initiator.isOnline()
                || targetId == null
                || targetId.equals(initiator.getUniqueId())
                || !canRevive(targetId)
                || pendingRevival.contains(targetId)
                || pendingInProgress.contains(targetId)
                || finishingRevival.contains(targetId)
                || activeRituals.containsKey(targetId)
                || pendingRefunds.containsKey(targetId)
                || resurrection == null
                || resurrection.isResurrecting(targetId)
                || plugin.getSoulFragmentRitual().isRitualActive(targetId)
                || !hasRequirements(initiator)) {
            return false;
        }

        Location altar = plugin.getAltarManager().getSummoningAltar();

        if (altar == null || altar.getWorld() == null) {
            initiator.sendMessage("§cLokasi Altar Pemanggilan belum diatur.");
            return false;
        }

        List<ItemStack> refundItems = createRefundItems();
        if (!consumeRequirements(initiator)) return false;

        pendingRefunds.put(
                targetId,
                new RefundData(initiator.getUniqueId(), refundItems)
        );
        save();

        initiator.sendMessage("§6Ritual kebangkitan dimulai. Waktu: 3 menit.");

        int[] elapsed = {0};

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
                    playRitualEffects(altar, elapsed[0]);

                    if (elapsed[0] % 30 == 0 && initiator.isOnline()) {
                        initiator.sendMessage(
                                "§eRitual kebangkitan: §f"
                                        + (RITUAL_SECONDS - elapsed[0])
                                        + " detik tersisa."
                        );
                    }

                    if (elapsed[0] >= RITUAL_SECONDS) {
                        finishRitual(initiator, targetId);
                    }
                },
                0L,
                20L
        );

        activeRituals.put(targetId, task);
        return true;
    }

    private List<ItemStack> createRefundItems() {
        List<ItemStack> items = new ArrayList<>();

        for (Requirement requirement : requirements) {
            ItemStack template = requirement.getItem();
            int remaining = requirement.getAmount();

            while (remaining > 0) {
                int amount = Math.min(remaining, template.getMaxStackSize());
                ItemStack refund = template.clone();
                refund.setAmount(amount);
                items.add(refund);
                remaining -= amount;
            }
        }
        return items;
    }
        private void finishRitual(Player initiator, UUID targetId) {
        BukkitTask task = activeRituals.remove(targetId);
        if (task != null) task.cancel();

        if (!canRevive(targetId)) {
            refundForTarget(targetId);
            return;
        }

        if (!finishingRevival.add(targetId)) return;

        Player target = Bukkit.getPlayer(targetId);

        if (target != null && target.isOnline()) {
            resurrection.resurrect(target, success -> {
                finishingRevival.remove(targetId);

                if (!success) {
                    refundForTarget(targetId);
                    if (initiator.isOnline()) {
                        initiator.sendMessage(
                                "§cKebangkitan gagal. Item persyaratan dikembalikan."
                        );
                    }
                    return;
                }

                teleportToSummoningAltar(target);

                pendingRefunds.remove(targetId);
                save();
                plugin.getSoulFragmentRitual().cancelRitual(targetId);

                if (initiator.isOnline()) {
                    initiator.sendMessage(
                            "§aRitual selesai! Target berhasil dibangkitkan di Altar Pemanggilan."
                    );
                }
            });
            return;
        }

        cleanupOfflineTarget(targetId);
        pendingRevival.add(targetId);
        pendingRefunds.remove(targetId);
        finishingRevival.remove(targetId);
        save();

        if (initiator.isOnline()) {
            initiator.sendMessage(
                    "§aRitual berhasil! Target akan dibangkitkan di Altar Pemanggilan saat masuk server."
            );
        }
    }

    private void teleportToSummoningAltar(Player player) {
        if (player == null || !player.isOnline()) return;

        Location altar = plugin.getAltarManager().getSummoningAltar();

        if (altar == null || altar.getWorld() == null) {
            player.sendMessage(
                    "§cAltar Pemanggilan tidak ditemukan. Kamu tetap berada di lokasi kebangkitan biasa."
            );
            plugin.getLogger().warning(
                    "Gagal memindahkan " + player.getName()
                            + ": lokasi Altar Pemanggilan tidak ditemukan."
            );
            return;
        }

        Location destination = findSafeAltarLocation(altar);

        if (destination == null) {
            player.sendMessage(
                    "§cLokasi Altar Pemanggilan tidak aman. Kamu tetap berada di lokasi kebangkitan biasa."
            );
            plugin.getLogger().warning(
                    "Tidak ditemukan lokasi aman di atas Altar Pemanggilan."
            );
            return;
        }

        if (player.teleport(destination)) {
            player.setFallDistance(0);
            player.sendMessage(
                    "§dJiwamu telah dipanggil kembali ke Altar Pemanggilan!"
            );
        } else {
            player.sendMessage(
                    "§cGagal memindahkanmu ke Altar Pemanggilan."
            );
        }
    }

    private Location findSafeAltarLocation(Location altar) {
        if (altar == null || altar.getWorld() == null) return null;

        World world = altar.getWorld();

        for (int height = 1; height <= 5; height++) {
            Location candidate = altar.clone().add(
                    0.5,
                    height,
                    0.5
            );

            Block feet = candidate.getBlock();
            Block head = candidate.clone().add(0, 1, 0).getBlock();
            Block ground = candidate.clone().subtract(0, 1, 0).getBlock();

            if (!world.getWorldBorder().isInside(candidate)) continue;
            if (!feet.isPassable() || !head.isPassable()) continue;

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

    private void cleanupOfflineTarget(UUID targetId) {
        plugin.getSoulFragmentDisplay().removeFragments(targetId);
        plugin.getSoulFragmentManager().resetTrial(targetId);
    }

    public void cancelRitualForTarget(UUID targetId) {
        if (targetId == null || finishingRevival.contains(targetId)) return;

        BukkitTask task = activeRituals.remove(targetId);
        if (task != null) task.cancel();

        refundForTarget(targetId);
    }

    private void cancelRitual(UUID targetId) {
        cancelRitualForTarget(targetId);
    }

    private void refundForTarget(UUID targetId) {
        if (targetId == null) return;

        RefundData refund = pendingRefunds.get(targetId);
        if (refund == null) return;

        Player initiator = Bukkit.getPlayer(refund.initiatorId);

        if (initiator == null || !initiator.isOnline()) {
            save();
            return;
        }

        giveItems(initiator, refund.items);
        pendingRefunds.remove(targetId);
        save();

        initiator.sendMessage(
                "§aItem persyaratan ritual yang dibatalkan telah dikembalikan."
        );
    }

    private void giveItems(Player player, List<ItemStack> items) {
        for (ItemStack original : items) {
            if (original == null || original.getType() == Material.AIR) continue;

            Map<Integer, ItemStack> leftovers =
                    player.getInventory().addItem(original.clone());

            for (ItemStack leftover : leftovers.values()) {
                Location location = player.getLocation();
                if (location.getWorld() != null) {
                    location.getWorld().dropItemNaturally(location, leftover);
                }
            }
        }
        player.updateInventory();
    }

    private void playRitualEffects(Location altar, int elapsed) {
        int stage = elapsed / 45;
        Location center = altar.clone().add(0.5, 1.2, 0.5);

        altar.getWorld().spawnParticle(
                Particle.SOUL, center, 4 + stage * 3,
                0.6, 0.5, 0.6, 0.03
        );

        if (elapsed % 3 == 0) {
            altar.getWorld().spawnParticle(
                    Particle.END_ROD, center, 2 + stage * 2,
                    0.8, 0.8, 0.8, 0.02
            );
        }

        if (elapsed % 10 == 0) {
            altar.getWorld().playSound(
                    center, Sound.BLOCK_BEACON_AMBIENT,
                    0.5f + stage * 0.2f,
                    0.7f + stage * 0.15f
            );
        }

        if (elapsed == 150 || elapsed == 180) {
            altar.getWorld().spawnParticle(
                    Particle.PORTAL, center, 80,
                    1.5, 1.5, 1.5, 0.5
            );
            altar.getWorld().playSound(
                    center, Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f
            );
        }
            }
        @EventHandler(priority = EventPriority.MONITOR)
    public void onTargetJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        refundForInitiator(uuid);

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

            resurrection.resurrect(player, success -> {
                pendingInProgress.remove(uuid);

                if (!success) {
                    if (player.isOnline()) {
                        player.sendMessage(
                                "§cKebangkitan tertunda belum berhasil. Silakan coba masuk kembali."
                        );
                    }
                    return;
                }

                teleportToSummoningAltar(player);

                pendingRevival.remove(uuid);
                save();

                if (player.isOnline()) {
                    player.sendMessage(
                            "§aRitual temanmu berhasil. Kamu telah kembali melalui Altar Pemanggilan!"
                    );
                }
            });
        });
    }

    private void refundForInitiator(UUID initiatorId) {
        if (initiatorId == null) return;

        Player player = Bukkit.getPlayer(initiatorId);
        if (player == null || !player.isOnline()) return;

        List<UUID> completedRefunds = new ArrayList<>();

        for (Map.Entry<UUID, RefundData> entry : pendingRefunds.entrySet()) {
            UUID targetId = entry.getKey();

            if (activeRituals.containsKey(targetId)
                    || finishingRevival.contains(targetId)) {
                continue;
            }

            if (entry.getValue().initiatorId.equals(initiatorId)) {
                giveItems(player, entry.getValue().items);
                completedRefunds.add(targetId);
            }
        }

        if (!completedRefunds.isEmpty()) {
            for (UUID targetId : completedRefunds) {
                pendingRefunds.remove(targetId);
            }

            save();
            player.sendMessage(
                    "§aItem persyaratan dari ritual yang dibatalkan telah dikembalikan."
            );
        }
    }

    private int countItem(Player player, ItemStack template) {
        int total = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item != null && item.isSimilar(template)) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeItems(Player player, ItemStack template, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getStorageContents();

        for (int slot = 0; slot < contents.length && remaining > 0; slot++) {
            ItemStack item = contents[slot];

            if (item == null || !item.isSimilar(template)) continue;

            int taken = Math.min(remaining, item.getAmount());
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

        YamlConfiguration config = new YamlConfiguration();

        for (int i = 0; i < requirements.size(); i++) {
            Requirement requirement = requirements.get(i);
            config.set("requirements." + i + ".item", requirement.getItem());
            config.set("requirements." + i + ".amount", requirement.getAmount());
        }

        for (UUID uuid : pendingRevival) {
            config.set("pending." + uuid, true);
        }

        for (Map.Entry<UUID, RefundData> entry : pendingRefunds.entrySet()) {
            String path = "refunds." + entry.getKey();

            config.set(path + ".initiator", entry.getValue().initiatorId.toString());

            for (int i = 0; i < entry.getValue().items.size(); i++) {
                config.set(path + ".items." + i, entry.getValue().items.get(i));
            }
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan data ritual: " + exception.getMessage()
            );
        }
    }

    private void load() {
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

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

                if (item == null || item.getType() == Material.AIR || amount <= 0) {
                    continue;
                }

                item.setAmount(1);
                requirements.add(new Requirement(item, amount));
            }
        }

        ConfigurationSection pending =
                config.getConfigurationSection("pending");

        if (pending != null) {
            for (String key : pending.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    if (pending.getBoolean(key)) pendingRevival.add(uuid);
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning(
                            "UUID pending revival tidak valid: " + key
                    );
                }
            }
        }

        ConfigurationSection refunds =
                config.getConfigurationSection("refunds");

        if (refunds != null) {
            for (String targetKey : refunds.getKeys(false)) {
                try {
                    UUID targetId = UUID.fromString(targetKey);
                    String initiatorValue =
                            refunds.getString(targetKey + ".initiator");

                    if (initiatorValue == null) continue;

                    UUID initiatorId = UUID.fromString(initiatorValue);
                    List<ItemStack> items = new ArrayList<>();

                    ConfigurationSection itemSection =
                            refunds.getConfigurationSection(targetKey + ".items");

                    if (itemSection != null) {
                        for (String itemKey : itemSection.getKeys(false)) {
                            ItemStack item = refunds.getItemStack(
                                    targetKey + ".items." + itemKey
                            );

                            if (item != null && item.getType() != Material.AIR) {
                                items.add(item);
                            }
                        }
                    }

                    if (!items.isEmpty()) {
                        pendingRefunds.put(
                                targetId,
                                new RefundData(initiatorId, items)
                        );
                    }
                } catch (IllegalArgumentException exception) {
                    plugin.getLogger().warning(
                            "Data refund tidak valid untuk target: " + targetKey
                    );
                }
            }
        }
    }

    public void stop() {
        for (BukkitTask task : activeRituals.values()) {
            if (task != null) task.cancel();
        }

        activeRituals.clear();
        save();
    }

    public static class Requirement {
        private final ItemStack item;
        private final int amount;

        private Requirement(ItemStack item, int amount) {
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

    private static class RefundData {
        private final UUID initiatorId;
        private final List<ItemStack> items;

        private RefundData(UUID initiatorId, List<ItemStack> items) {
            this.initiatorId = initiatorId;
            this.items = new ArrayList<>();

            for (ItemStack item : items) {
                if (item != null && item.getType() != Material.AIR) {
                    this.items.add(item.clone());
                }
            }
        }
    }
            }
