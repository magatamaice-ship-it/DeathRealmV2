
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DeathRealmSoulFragmentDisplay {

    private final DeathRealm plugin;
    private final DeathRealmSoulFragmentManager fragmentManager;

    private final NamespacedKey ownerKey;
    private final NamespacedKey fragmentKey;

    private final Map<UUID, List<Location>> activeLocations =
            new HashMap<>();

    private BukkitTask particleTask;

    public DeathRealmSoulFragmentDisplay(
            DeathRealm plugin,
            DeathRealmSoulFragmentManager fragmentManager
    ) {
        this.plugin = plugin;
        this.fragmentManager = fragmentManager;

        ownerKey = new NamespacedKey(plugin, "soul_owner");
        fragmentKey = new NamespacedKey(plugin, "soul_fragment");
    }

    public void spawnFragments(UUID owner, String ownerName) {
        if (!fragmentManager.isTrialActive(owner)) {
            return;
        }

        List<Location> locations =
                fragmentManager.getLocations(owner);

        if (locations.size() != 3) {
            plugin.getLogger().warning(
                    "Lokasi Fragmen Jiwa tidak lengkap untuk " + owner
            );
            return;
        }

        removeFragments(owner);

        for (int i = 0; i < locations.size(); i++) {
            if (fragmentManager.hasCollected(owner, i)) {
                continue;
            }

            Location location = locations.get(i);

            if (location.getWorld() == null) {
                continue;
            }

            spawnSingleFragment(owner, ownerName, i, location);
        }

        activeLocations.put(owner, locations);
        startParticleTask();
    }

    private void spawnSingleFragment(
            UUID owner,
            String ownerName,
            int index,
            Location location
    ) {
        removeExistingFragment(owner, index, location);

        Location itemLocation = location.clone();

        ItemDisplay item = location.getWorld().spawn(
                itemLocation,
                ItemDisplay.class
        );

        item.setItemStack(
                new ItemStack(Material.AMETHYST_SHARD)
        );

        item.setBillboard(Display.Billboard.CENTER);
        item.setGravity(false);
        item.setInvulnerable(true);

        setIdentity(item, owner, index);

        Location nameLocation = location.clone().add(0, 1.1, 0);

        TextDisplay name = location.getWorld().spawn(
                nameLocation,
                TextDisplay.class
        );

        name.setText(
                "§d§lFragmen Jiwa\n"
                + "§7Milik: §f" + ownerName
        );

        name.setBillboard(Display.Billboard.CENTER);
        name.setSeeThrough(true);
        name.setShadowed(true);
        name.setDefaultBackground(false);
        name.setAlignment(TextDisplay.TextAlignment.CENTER);

        setIdentity(name, owner, index);

        Interaction interaction = location.getWorld().spawn(
                location.clone(),
                Interaction.class
        );

        interaction.setInteractionWidth(1.0F);
        interaction.setInteractionHeight(1.5F);
        interaction.setResponsive(true);

        setIdentity(interaction, owner, index);
    }

    private void setIdentity(
            Entity entity,
            UUID owner,
            int index
    ) {
        entity.getPersistentDataContainer().set(
                ownerKey,
                PersistentDataType.STRING,
                owner.toString()
        );

        entity.getPersistentDataContainer().set(
                fragmentKey,
                PersistentDataType.INTEGER,
                index
        );
    }

    private void removeExistingFragment(
            UUID owner,
            int index,
            Location location
    ) {
        if (location.getWorld() == null) {
            return;
        }

        for (Entity entity :
                location.getChunk().getEntities()) {

            String savedOwner =
                    entity.getPersistentDataContainer().get(
                            ownerKey,
                            PersistentDataType.STRING
                    );

            Integer savedIndex =
                    entity.getPersistentDataContainer().get(
                            fragmentKey,
                            PersistentDataType.INTEGER
                    );

            if (owner.toString().equals(savedOwner)
                    && savedIndex != null
                    && savedIndex == index) {
                entity.remove();
            }
        }
    }

    public void removeFragment(UUID owner, int index) {
        List<Location> locations =
                fragmentManager.getLocations(owner);

        if (index < 0 || index >= locations.size()) {
            return;
        }

        removeExistingFragment(
                owner,
                index,
                locations.get(index)
        );
    }

    public void removeFragments(UUID owner) {
        List<Location> locations =
                fragmentManager.getLocations(owner);

        for (int i = 0; i < locations.size(); i++) {
            removeExistingFragment(owner, i, locations.get(i));
        }

        activeLocations.remove(owner);
    }

    private void startParticleTask() {
        if (particleTask != null) {
            return;
        }

        particleTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                () -> {
                    for (Map.Entry<UUID, List<Location>> entry :
                            activeLocations.entrySet()) {

                        UUID owner = entry.getKey();

                        for (int i = 0;
                             i < entry.getValue().size(); i++) {

                            if (fragmentManager.hasCollected(owner, i)) {
                                continue;
                            }

                            Location base = entry.getValue().get(i);

                            if (base.getWorld() == null) {
                                continue;
                            }

                            for (double y = 0.3; y <= 3.0; y += 0.3) {
                                base.getWorld().spawnParticle(
                                        Particle.END_ROD,
                                        base.getX(),
                                        base.getY() + y,
                                        base.getZ(),
                                        1,
                                        0,
                                        0,
                                        0,
                                        0
                                );
                            }
                        }
                    }
                },
                1L,
                10L
        );
    }

    public void stop() {
        if (particleTask != null) {
            particleTask.cancel();
            particleTask = null;
        }

        activeLocations.clear();
    }
          }
