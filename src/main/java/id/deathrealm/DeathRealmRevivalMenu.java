
package id.deathrealm;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathRealmRevivalMenu implements Listener {

    private static final int ITEMS_PER_PAGE = 45;

    private final DeathRealm plugin;
    private final DeathTimerManager timerManager;
    private final DeathRealmFriendRevivalManager revivalManager;

    public DeathRealmRevivalMenu(DeathRealm plugin) {
        this.plugin = plugin;
        this.timerManager = plugin.getTimerManager();
        this.revivalManager = plugin.getFriendRevivalManager();
    }

    public void openMenu(Player player, int page) {
        List<OfflinePlayer> deadPlayers = new ArrayList<>();

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (timerManager.hasTimer(offlinePlayer.getUniqueId())
                    && !offlinePlayer.getUniqueId()
                    .equals(player.getUniqueId())) {
                deadPlayers.add(offlinePlayer);
            }
        }

        deadPlayers.sort((first, second) -> {
            String firstName = first.getName() == null
                    ? "" : first.getName();

            String secondName = second.getName() == null
                    ? "" : second.getName();

            return firstName.compareToIgnoreCase(secondName);
        });

        int totalPages = Math.max(
                1,
                (int) Math.ceil(
                        deadPlayers.size() / (double) ITEMS_PER_PAGE
                )
        );

        if (page < 0 || page >= totalPages) {
            page = 0;
        }

        MenuHolder holder = new MenuHolder(page, false, null);

        Inventory inventory = Bukkit.createInventory(
                holder,
                54,
                Component.text("Altar Pemanggilan")
        );

        holder.setInventory(inventory);

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(
                start + ITEMS_PER_PAGE,
                deadPlayers.size()
        );

        for (int i = start; i < end; i++) {
            OfflinePlayer target = deadPlayers.get(i);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();

            if (meta == null) {
                continue;
            }

            meta.setOwningPlayer(target);

            String name = target.getName() == null
                    ? "Pemain tidak dikenal"
                    : target.getName();

            meta.displayName(Component.text("§e" + name));
            meta.lore(List.of(
                    Component.text("§7Status: §cMati"),
                    Component.text("§aKlik untuk memilih pemain ini")
            ));

            head.setItemMeta(meta);

            inventory.setItem(i - start, head);
            holder.addTarget(i - start, target.getUniqueId());
        }

        if (deadPlayers.isEmpty()) {
            inventory.setItem(
                    22,
                    createItem(
                            Material.BARRIER,
                            "§cTidak ada pemain mati",
                            "§7Belum ada pemain yang dapat dibangkitkan."
                    )
            );
        }

        if (page > 0) {
            inventory.setItem(
                    45,
                    createItem(
                            Material.ARROW,
                            "§eHalaman sebelumnya"
                    )
            );
        }

        inventory.setItem(
                49,
                createItem(Material.BARRIER, "§cTutup menu")
        );

        if (page < totalPages - 1) {
            inventory.setItem(
                    53,
                    createItem(
                            Material.ARROW,
                            "§eHalaman berikutnya"
                    )
            );
        }

        inventory.setItem(
                48,
                createItem(
                        Material.PAPER,
                        "§bInformasi halaman",
                        "§7Halaman: §f" + (page + 1)
                                + "§7/§f" + totalPages,
                        "§7Pemain mati: §f" + deadPlayers.size()
                )
        );

        player.openInventory(inventory);
    }

    private void openConfirmation(Player player, UUID targetId) {
        if (!revivalManager.canRevive(targetId)) {
            player.sendMessage(
                    "§cPemain tersebut sudah tidak berstatus mati."
            );
            openMenu(player, 0);
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);

        String name = target.getName() == null
                ? "Pemain tidak dikenal"
                : target.getName();

        MenuHolder holder = new MenuHolder(0, true, targetId);

        Inventory inventory = Bukkit.createInventory(
                holder,
                27,
                Component.text("Konfirmasi Kebangkitan")
        );

        holder.setInventory(inventory);

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(target);
            skullMeta.displayName(Component.text("§e" + name));
            head.setItemMeta(skullMeta);
        }

        inventory.setItem(4, head);

        inventory.setItem(
                11,
                createItem(
                        Material.LIME_CONCRETE,
                        "§aKONFIRMASI RITUAL",
                        "§7Mulai ritual kebangkitan.",
                        "§7Durasi: §e3 menit",
                        "§7Persyaratan item akan dikonsumsi."
                )
        );

        inventory.setItem(
                15,
                createItem(
                        Material.RED_CONCRETE,
                        "§cBATAL",
                        "§7Kembali ke daftar pemain."
                )
        );

        if (!revivalManager.hasRequirements(player)) {
            inventory.setItem(
                    22,
                    createItem(
                            Material.BARRIER,
                            "§cPersyaratan belum cukup",
                            "§7Periksa item yang dibutuhkan."
                    )
            );
        } else {
            inventory.setItem(
                    22,
                    createItem(
                            Material.EMERALD,
                            "§aPersyaratan terpenuhi",
                            "§7Kamu dapat memulai ritual."
                    )
            );
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (!(event.getInventory().getHolder()
                instanceof MenuHolder holder)) {
            return;
        }

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slot < 0
                || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (holder.isConfirmation()) {
            handleConfirmationClick(player, holder, slot);
            return;
        }

        if (slot == 49) {
            player.closeInventory();
            return;
        }

        if (slot == 45) {
            openMenu(player, holder.getPage() - 1);
            return;
        }

        if (slot == 53) {
            openMenu(player, holder.getPage() + 1);
            return;
        }

        UUID targetId = holder.getTarget(slot);

        if (targetId != null) {
            openConfirmation(player, targetId);
        }
    }

    private void handleConfirmationClick(
            Player player,
            MenuHolder holder,
            int slot
    ) {
        if (slot == 15) {
            openMenu(player, 0);
            return;
        }

        if (slot != 11) {
            return;
        }

        UUID targetId = holder.getSelectedTarget();

        if (targetId == null
                || !revivalManager.canRevive(targetId)) {
            player.sendMessage(
                    "§cTarget sudah tidak dapat dibangkitkan."
            );
            player.closeInventory();
            return;
        }

        if (!revivalManager.hasRequirements(player)) {
            player.sendMessage(
                    "§cItem persyaratan ritual belum mencukupi."
            );
            return;
        }

        if (revivalManager.startRitual(player, targetId)) {
            player.closeInventory();
            player.sendMessage(
                    "§aRitual kebangkitan berhasil dimulai!"
            );
        } else {
            player.sendMessage(
                    "§cRitual gagal dimulai. Periksa status target dan persyaratan."
            );
        }
    }

    private ItemStack createItem(
            Material material,
            String name,
            String... lore
    ) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(name));

            List<Component> lines = new ArrayList<>();

            for (String line : lore) {
                lines.add(Component.text(line));
            }

            meta.lore(lines);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static class MenuHolder implements InventoryHolder {

        private final int page;
        private final boolean confirmation;
        private final UUID selectedTarget;
        private final Map<Integer, UUID> targets = new HashMap<>();

        private Inventory inventory;

        private MenuHolder(
                int page,
                boolean confirmation,
                UUID selectedTarget
        ) {
            this.page = page;
            this.confirmation = confirmation;
            this.selectedTarget = selectedTarget;
        }

        public int getPage() {
            return page;
        }

        public boolean isConfirmation() {
            return confirmation;
        }

        public UUID getSelectedTarget() {
            return selectedTarget;
        }

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        public void addTarget(int slot, UUID uuid) {
            targets.put(slot, uuid);
        }

        public UUID getTarget(int slot) {
            return targets.get(slot);
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
