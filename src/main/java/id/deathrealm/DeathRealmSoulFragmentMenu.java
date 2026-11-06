
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class DeathRealmSoulFragmentMenu implements Listener {

    private final DeathRealm plugin;
    private final DeathRealmSoulFragmentManager fragmentManager;

    public DeathRealmSoulFragmentMenu(
            DeathRealm plugin,
            DeathRealmSoulFragmentManager fragmentManager
    ) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;
    }

    public void openMenu(Player player) {
        if (!plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            player.sendMessage(
                    "§cKamu tidak sedang berada di ALAMKACUNG."
            );
            return;
        }

        if (!plugin.getTimerManager().isTrialUnlocked(
                player.getUniqueId()
        )) {
            player.sendMessage(
                    "§cUjian kebangkitanmu belum terbuka."
            );
            return;
        }

        MenuHolder holder = new MenuHolder();

        Inventory inventory = Bukkit.createInventory(
                holder,
                27,
                "§5§lUJIAN KEBANGKITAN"
        );

        holder.setInventory(inventory);

        ItemStack background = new ItemStack(
                Material.PURPLE_STAINED_GLASS_PANE
        );

        ItemMeta backgroundMeta = background.getItemMeta();
        backgroundMeta.setDisplayName(" ");
        background.setItemMeta(backgroundMeta);

        for (int slot = 0; slot < 27; slot++) {
            inventory.setItem(slot, background);
        }

        ItemStack button = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = button.getItemMeta();

        boolean active = fragmentManager.isTrialActive(
                player.getUniqueId()
        );

        if (active) {
            meta.setDisplayName("§a§lLANJUTKAN PENCARIAN");
            meta.setLore(Arrays.asList(
                    "§7Ujian kebangkitanmu sudah dimulai.",
                    "",
                    "§eFragmen terkumpul: §f"
                            + fragmentManager.getCollectedCount(
                                    player.getUniqueId()
                            ) + "/3",
                    "",
                    "§aKlik untuk melanjutkan."
            ));
        } else {
            meta.setDisplayName("§d§lMULAI TAHAP 2");
            meta.setLore(Arrays.asList(
                    "§7Mulai pencarian tiga Fragmen Jiwa.",
                    "",
                    "§7Fragmen akan muncul di",
                    "§7lokasi acak di ALAMKACUNG.",
                    "",
                    "§eKlik untuk memulai ujian."
            ));
        }

        button.setItemMeta(meta);
        inventory.setItem(13, button);

        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder()
                instanceof MenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (event.getRawSlot() != 13) {
            return;
        }

        if (!plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        ) || !plugin.getTimerManager().isTrialUnlocked(
                player.getUniqueId()
        )) {
            player.closeInventory();
            player.sendMessage(
                    "§cUjian kebangkitan tidak tersedia."
            );
            return;
        }

        if (fragmentManager.isTrialActive(
                player.getUniqueId()
        )) {
            player.closeInventory();

            plugin.getSoulFragmentDisplay().spawnFragments(
                    player.getUniqueId(),
                    player.getName()
            );

            player.sendMessage(
                    "§5[ALAMKACUNG] §dPencarian Fragmen Jiwa dilanjutkan."
            );

            player.sendMessage(
                    "§7Fragmen terkumpul: §f"
                            + fragmentManager.getCollectedCount(
                                    player.getUniqueId()
                            ) + "/3"
            );

            return;
        }

        boolean started = fragmentManager.startTrial(
                player.getUniqueId()
        );

        player.closeInventory();

        if (!started) {
            player.sendMessage(
                    "§cGagal memulai Tahap 2. Coba buka GUI kembali."
            );
            return;
        }

        plugin.getSoulFragmentDisplay().spawnFragments(
                player.getUniqueId(),
                player.getName()
        );

        player.sendTitle(
                "§5TAHAP 2 DIMULAI",
                "§dTemukan tiga Fragmen Jiwa",
                10,
                70,
                20
        );

        player.sendMessage(
                "§5[ALAMKACUNG] §dPencarian tiga Fragmen Jiwa dimulai!"
        );
    }

    private static class MenuHolder implements InventoryHolder {

        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}
