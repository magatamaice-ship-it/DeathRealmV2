
package id.deathrealm;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathRealmAltarCommand implements CommandExecutor {

    private final DeathRealmAltarManager altarManager;

    public DeathRealmAltarCommand(
            DeathRealmAltarManager altarManager
    ) {
        this.altarManager = altarManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Perintah ini hanya bisa digunakan pemain.");
            return true;
        }

        if (!player.hasPermission("deathrealm.admin")) {
            player.sendMessage("§cKamu tidak memiliki izin.");
            return true;
        }

        if (args.length != 1) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setpemanggilan":
                if (!player.getWorld().getName()
                        .equalsIgnoreCase("DeathRealm")) {
                    player.sendMessage(
                            "§cKamu harus berada di ALAMKACUNG."
                    );
                    return true;
                }

                altarManager.setSummoningAltar(
                        player.getLocation()
                );

                player.sendMessage(
                        "§aAltar Pemanggilan berhasil disimpan!"
                );
                break;

            case "setkelahiran":
                if (!player.getWorld().getName()
                        .equalsIgnoreCase("DeathRealm")) {
                    player.sendMessage(
                            "§cKamu harus berada di ALAMKACUNG."
                    );
                    return true;
                }

                altarManager.setRebirthAltar(
                        player.getLocation()
                );

                player.sendMessage(
                        "§aAltar Kelahiran berhasil disimpan!"
                );
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§5===== ALAMKACUNG =====");
        player.sendMessage("§d/alamkacung setpemanggilan");
        player.sendMessage("§d/alamkacung setkelahiran");
    }
}
