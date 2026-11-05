package id.deathrealm;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;

public class DeathRealmCommandProtection implements Listener {

    private final DeathRealm plugin;

    private final Set<String> blockedCommands = Set.of(
            "tp",
            "teleport",
            "tpa",
            "tpaccept",
            "home",
            "homes",
            "spawn",
            "warp",
            "back"
    );

    public DeathRealmCommandProtection(DeathRealm plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            return;
        }

        String message = event.getMessage()
                .substring(1)
                .split(" ")[0]
                .toLowerCase();

        if (message.contains(":")) {
            message = message.substring(
                    message.indexOf(':') + 1
            );
        }

        if (blockedCommands.contains(message)) {
            event.setCancelled(true);

            player.sendMessage(
                    "§cKamu tidak bisa menggunakan perintah tersebut selama berada dalam masa kematian!"
            );
        }
    }
}
