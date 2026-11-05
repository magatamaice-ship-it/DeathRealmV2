package id.deathrealm;

import org.bukkit.World;
import org.bukkit.WorldBorder;

public class DeathRealmBoundary {

    private final DeathRealm plugin;
    private final World world;

    private static final double BORDER_SIZE = 220.0;

    public DeathRealmBoundary(DeathRealm plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    public void applyBoundary() {
        if (world == null) {
            plugin.getLogger().warning(
                    "Tidak bisa memasang pembatas: dunia DeathRealm tidak tersedia!"
            );
            return;
        }

        WorldBorder border = world.getWorldBorder();

        border.setCenter(0.0, 0.0);
        border.setSize(BORDER_SIZE);
        border.setWarningDistance(5);
        border.setWarningTime(0);

        plugin.getLogger().info(
                "Pembatas DeathRealm 220x220 berhasil dipasang!"
        );
    }
}
