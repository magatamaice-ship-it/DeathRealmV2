
package id.deathrealm;

import org.bukkit.Location;
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
    private final DeathRealmRevivalMenu revivalMenu;

    public DeathRealmAltarListener(
            DeathRealm plugin,
            DeathRealmAltarManager altarManager
    ) {
        this.plugin = plugin;
        this.altarManager = altarManager;
        this.revivalMenu = new DeathRealmRevivalMenu(plugin);

        plugin.getServer().getPluginManager().registerEvents(
                revivalMenu,
                plugin
        );
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

            player.sendMessage("§6§lALTAR PEMANGGILAN");
            revivalMenu.openMenu(player, 0);

            return;
        }

        if (altarManager.isRebirthAltar(location)) {
            event.setCancelled(true);

            player.sendMessage("§5§lALTAR KELAHIRAN");

            player.sendMessage(
                    "§7Periksa tiga Fragmen Jiwa untuk ritual kebangkitan."
            );
        }
    }
}
