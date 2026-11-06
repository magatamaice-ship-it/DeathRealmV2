
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DeathRealmAltarListener implements Listener {

    private final DeathRealm plugin;
    private final DeathRealmAltarManager altarManager;

    public DeathRealmAltarListener(
            DeathRealm plugin,
            DeathRealmAltarManager altarManager
    ) {
        this.plugin = plugin;
        this.altarManager = altarManager;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAltarInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        Player player = event.getPlayer();
        Location location = block.getLocation();

        if (altarManager.isSummoningAltar(location)) {
            event.setCancelled(true);

            player.sendMessage(
                    "§6§lALTAR PEMANGGILAN"
            );

            player.sendMessage(
                    "§7Altar terdeteksi. Sistem ritual kebangkitan sedang disiapkan."
            );

            return;
        }

        if (altarManager.isRebirthAltar(location)) {
            event.setCancelled(true);

            player.sendMessage(
                    "§5§lALTAR KELAHIRAN"
            );

            player.sendMessage(
                    "§7Altar terdeteksi. Periksa tiga Fragmen Jiwa untuk ritual kebangkitan."
            );
        }
    }
}
