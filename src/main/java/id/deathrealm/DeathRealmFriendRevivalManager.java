
package id.deathrealm;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeathRealmFriendRevivalManager {

    private final DeathRealm plugin;
    private final File file;
    private final List<Requirement> requirements = new ArrayList<>();

    public DeathRealmFriendRevivalManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(
                plugin.getDataFolder(),
                "revival-requirements.yml"
        );

        load();
    }

    public boolean addRequirement(Player player, int amount) {
        if (player == null || amount <= 0) {
            return false;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

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

        requirements.add(new Requirement(savedItem, amount));
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
            if (countItem(player, requirement.getItem())
                    < requirement.getAmount()) {
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

    private int countItem(Player player, ItemStack template) {
        int total = 0;

        for (ItemStack item : player.getInventory().getStorageContents()) {
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
             slot < player.getInventory().getStorageContents().length
                     && remaining > 0;
             slot++) {

            ItemStack item = player.getInventory().getItem(slot);

            if (item == null || !item.isSimilar(template)) {
                continue;
            }

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

            config.set(
                    "requirements." + i + ".item",
                    requirement.getItem()
            );

            config.set(
                    "requirements." + i + ".amount",
                    requirement.getAmount()
            );
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

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            ItemStack item = config.getItemStack(
                    "requirements." + key + ".item"
            );

            int amount = config.getInt(
                    "requirements." + key + ".amount"
            );

            if (item == null || item.getType() == Material.AIR
                    || amount <= 0) {
                continue;
            }

            item.setAmount(1);
            requirements.add(new Requirement(item, amount));
        }

        plugin.getLogger().info(
                "Berhasil memuat " + requirements.size()
                + " persyaratan ritual kebangkitan."
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
