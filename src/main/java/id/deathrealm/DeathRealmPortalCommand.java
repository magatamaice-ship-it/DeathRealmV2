
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathRealmPortalCommand implements CommandExecutor {

    private final DeathRealmPortalManager portalManager;

    public DeathRealmPortalCommand(DeathRealmPortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Perintah ini hanya bisa digunakan pemain.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("deathrealm.admin")) {
            player.sendMessage("Kamu tidak memiliki izin.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§eGunakan:");
            player.sendMessage("§f/alamkacung setportalmasuk");
            player.sendMessage("§f/alamkacung setportalkeluar");
            return true;
        }

        Location location = player.getLocation();

        switch (args[0].toLowerCase()) {
            case "setportalmasuk":
                if (location.getWorld().getName().equalsIgnoreCase("DeathRealm")) {
                    player.sendMessage("§cPortal masuk harus berada di dunia utama!");
                    return true;
                }

                portalManager.setPortalEntry(location);
                player.sendMessage("§aLokasi portal masuk berhasil disimpan!");
                break;

            case "setportalkeluar":
                if (!location.getWorld().getName().equalsIgnoreCase("DeathRealm")) {
                    player.sendMessage("§cPortal keluar harus berada di ALAMKACUNG!");
                    return true;
                }

                portalManager.setPortalExit(location);
                player.sendMessage("§aLokasi portal keluar berhasil disimpan!");
                break;

            default:
                player.sendMessage("§cPerintah tidak dikenal.");
                break;
        }

        return true;
    }
}
