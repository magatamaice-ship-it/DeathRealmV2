
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

    public DeathRealmRevivalMenu(DeathRealm plugin) {
        this.plugin = plugin;
        this.timerManager = plugin.getTimerManager();
    }

    public void openMenu(Player player, int page) {
        List<OfflinePlayer> deadPlayers = new ArrayList<>();

        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (timerManager.hasTimer(offlinePlayer.getUniqueId())) {
                deadPlayers.add(offlinePlayer);
            }
        }

        deadPlayers.sort((first, second) -> {
            String firstName = first.getName() == null
                    ? ""
                    : first.getName();

            String secondName = second.getName() == null
                    ? ""
                    : second.getName();

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

        MenuHolder holder = new MenuHolder(page);

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

            meta.displayName(
                    Component.text("§e" + name)
            );

            meta.lore(List.of(
                    Component.text("§7Status: §cMati"),
                    Component.text(
                            "§aKlik untuk memilih pemain ini"
                    )
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
                            "§eHalaman sebelumnya",
                            "§7Klik untuk kembali."
                    )
            );
        }

        inventory.setItem(
                49,
                createItem(
                        Material.BARRIER,
                        "§cTutup",
                        "§7Tutup menu."
                )
        );

        if (page < totalPages - 1) {
            inventory.setItem(
                    53,
                    createItem(
                            Material.ARROW,
                            "§eHalaman berikutnya",
                            "§7Klik untuk melanjutkan."
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

        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
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

        if (targetId == null) {
            return;
        }

        if (!timerManager.hasTimer(targetId)) {
            player.sendMessage(
                    "§cPemain tersebut sudah tidak berstatus mati."
            );
            openMenu(player, holder.getPage());
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetId);

        String targetName = target.getName() == null
                ? targetId.toString()
                : target.getName();

        player.closeInventory();

        player.sendMessage(
                "§aKamu memilih §e" + targetName
                        + "§a untuk dibangkitkan."
        );

        player.sendMessage(
                "§7Persyaratan dan proses ritual akan diperiksa pada tahap berikutnya."
        );
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
        private final Map<Integer, UUID> targets = new HashMap<>();
        private Inventory inventory;

        private MenuHolder(int page) {
            this.page = page;
        }

        public int getPage() {
            return page;
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
