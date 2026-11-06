package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DeathRealmSoulFragmentManager {

    private static final int FRAGMENT_COUNT = 3;
    private static final int ISLAND_RADIUS = 65;
    private static final int CENTER_X = 0;
    private static final int CENTER_Z = 0;
    private static final int SURFACE_Y = 90;

    private final DeathRealm plugin;
    private final File file;
    private final Random random = new Random();

    private final Map<UUID, List<Location>> locations =
            new HashMap<>();

    private final Map<UUID, Set<Integer>> collected =
            new HashMap<>();

    private final Set<UUID> activeTrials =
            new HashSet<>();

    public DeathRealmSoulFragmentManager(DeathRealm plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "soul-fragments.yml");

        load();
    }

    public boolean startTrial(UUID uuid) {
        if (!plugin.getTimerManager().isTrialUnlocked(uuid)) {
            return false;
        }

        if (activeTrials.contains(uuid)) {
            return false;
        }

        List<Location> playerLocations = generateLocations();

        locations.put(uuid, playerLocations);
        collected.put(uuid, new HashSet<>());
        activeTrials.add(uuid);

        save();
        return true;
    }

    public boolean isTrialActive(UUID uuid) {
        return activeTrials.contains(uuid);
    }

    public List<Location> getLocations(UUID uuid) {
        List<Location> result = locations.get(uuid);

        if (result == null) {
            return Collections.emptyList();
        }

        return new ArrayList<>(result);
    }

    public boolean collectFragment(UUID uuid, int fragment) {
        if (!activeTrials.contains(uuid)) {
            return false;
        }

        if (fragment < 0 || fragment >= FRAGMENT_COUNT) {
            return false;
        }

        Set<Integer> playerCollected = collected.computeIfAbsent(
                uuid,
                key -> new HashSet<>()
        );

        if (!playerCollected.add(fragment)) {
            return false;
        }

        plugin.getTimerManager().addFragment(
                uuid,
                getFragmentKey(fragment)
        );

        save();
        return true;
    }

    public boolean hasCollected(UUID uuid, int fragment) {
        return collected.getOrDefault(
                uuid,
                Collections.emptySet()
        ).contains(fragment);
    }

    public int getCollectedCount(UUID uuid) {
        return collected.getOrDefault(
                uuid,
                Collections.emptySet()
        ).size();
    }

    public boolean hasAllFragments(UUID uuid) {
        return getCollectedCount(uuid) >= FRAGMENT_COUNT;
    }

    public void resetTrial(UUID uuid) {
        locations.remove(uuid);
        collected.remove(uuid);
        activeTrials.remove(uuid);
        save();
    }

    public void save() {
        YamlConfiguration config = new YamlConfiguration();

        for (UUID uuid : locations.keySet()) {
            String path = uuid.toString();

            config.set(path + ".active", activeTrials.contains(uuid));

            List<Map<String, Object>> savedLocations =
                    new ArrayList<>();

            List<Location> playerLocations = locations.get(uuid);

            for (Location location : playerLocations) {
                Map<String, Object> data = new HashMap<>();

                data.put("world", location.getWorld().getName());
                data.put("x", location.getX());
                data.put("y", location.getY());
                data.put("z", location.getZ());

                savedLocations.add(data);
            }

            config.set(path + ".locations", savedLocations);

            config.set(
                    path + ".collected",
                    new ArrayList<>(
                            collected.getOrDefault(
                                    uuid,
                                    Collections.emptySet()
                            )
                    )
            );
        }

        try {
            config.save(file);
        } catch (IOException exception) {
            plugin.getLogger().severe(
                    "Gagal menyimpan Soul Fragments: "
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

        for (String key : config.getKeys(false)) {
            UUID uuid;

            try {
                uuid = UUID.fromString(key);
            } catch (IllegalArgumentException exception) {
                continue;
            }

            List<Location> playerLocations = new ArrayList<>();

            for (Map<?, ?> data :
                    config.getMapList(key + ".locations")) {

                World world = plugin.getServer().getWorld(
                        String.valueOf(data.get("world"))
                );

                if (world == null) {
                    continue;
                }

                double x = ((Number) data.get("x")).doubleValue();
                double y = ((Number) data.get("y")).doubleValue();
                double z = ((Number) data.get("z")).doubleValue();

                playerLocations.add(
                        new Location(world, x, y, z)
                );
            }

            Set<Integer> playerCollected = new HashSet<>();

            for (Object value :
                    config.getList(key + ".collected",
                            Collections.emptyList())) {

                if (value instanceof Number number) {
                    playerCollected.add(number.intValue());
                }
            }

            if (playerLocations.size() == FRAGMENT_COUNT) {
                locations.put(uuid, playerLocations);
                collected.put(uuid, playerCollected);

                if (config.getBoolean(key + ".active")) {
                    activeTrials.add(uuid);
                }
            }
        }
    }

    private List<Location> generateLocations() {
        List<Location> result = new ArrayList<>();

        World world = plugin.getServer().getWorld("DeathRealm");

        if (world == null) {
            return result;
        }

        int attempts = 0;

        while (result.size() < FRAGMENT_COUNT && attempts < 1000) {
            attempts++;

            double angle = random.nextDouble() * Math.PI * 2;
            double distance = Math.sqrt(random.nextDouble())
                    * ISLAND_RADIUS;

            int x = CENTER_X + (int) Math.round(
                    Math.cos(angle) * distance
            );

            int z = CENTER_Z + (int) Math.round(
                    Math.sin(angle) * distance
            );

            Location location = new Location(
                    world,
                    x + 0.5,
                    SURFACE_Y + 1,
                    z + 0.5
            );

            if (!world.getBlockAt(x, SURFACE_Y, z).getType().isSolid()) {
                continue;
            }

            if (!world.getBlockAt(x, SURFACE_Y + 1, z)
                    .isPassable()) {
                continue;
            }

            boolean tooClose = false;

            for (Location existing : result) {
                if (existing.distanceSquared(location) < 400) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                result.add(location);
            }
        }

        return result;
    }

    private String getFragmentKey(int fragment) {
        return switch (fragment) {
            case 0 -> DeathTimerManager.ANCIENT;
            case 1 -> DeathTimerManager.CURSED;
            case 2 -> DeathTimerManager.LAST;
            default -> throw new IllegalArgumentException(
                    "Fragmen tidak valid."
            );
        };
    }
  }
