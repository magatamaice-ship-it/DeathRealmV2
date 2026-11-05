package id.deathrealm;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Set;
import java.util.Locale;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getTimerManager().hasTimer(
                player.getUniqueId()
        )) {
            return;
        }

        String message = event.getMessage().strip();

        if (!message.startsWith("/")) {
            return;
        }

        String[] parts = message.substring(1).split("\\s+", 2);

        if (parts.length == 0 || parts[0].isEmpty()) {
            return;
        }

        String command = parts[0].toLowerCase(Locale.ROOT);

        if (command.contains(":")) {
            command = command.substring(
                    command.lastIndexOf(':') + 1
            );
        }

        if (!blockedCommands.contains(command)) {
            return;
        }

        event.setCancelled(true);

        player.sendMessage(
                "§cKamu tidak bisa menggunakan perintah tersebut selama berada dalam masa kematian!"
        );
    }
}
