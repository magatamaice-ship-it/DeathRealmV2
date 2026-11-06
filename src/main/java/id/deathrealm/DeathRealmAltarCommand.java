
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathRealmAltarCommand implements CommandExecutor {

    private final DeathRealmAltarManager altarManager;
    private final DeathRealmPortalManager portalManager;

    public DeathRealmAltarCommand(
            DeathRealmAltarManager altarManager,
            DeathRealmPortalManager portalManager
    ) {
        this.altarManager = altarManager;
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
            player.sendMessage("§cKamu tidak memiliki izin.");
            return true;
        }

        if (args.length != 1) {
            player.sendMessage("§ePengaturan lokasi ALAMKACUNG:");
            player.sendMessage("§f/alamkacung setpemanggilan");
            player.sendMessage("§f/alamkacung setkelahiran");
            player.sendMessage("§f/alamkacung setportalmasuk");
            player.sendMessage("§f/alamkacung setportalkeluar");
            return true;
        }

        Location location = player.getLocation();

        switch (args[0].toLowerCase()) {

            case "setpemanggilan":
                if (!isDeathRealm(location)) {
                    player.sendMessage("§cAltar harus berada di ALAMKACUNG!");
                    return true;
                }

                altarManager.setSummoningAltar(location);
                player.sendMessage("§aLokasi Altar Pemanggilan berhasil disimpan!");
                break;

            case "setkelahiran":
                if (!isDeathRealm(location)) {
                    player.sendMessage("§cAltar harus berada di ALAMKACUNG!");
                    return true;
                }

                altarManager.setRebirthAltar(location);
                player.sendMessage("§aLokasi Altar Kelahiran berhasil disimpan!");
                break;

            case "setportalmasuk":
                if (isDeathRealm(location)) {
                    player.sendMessage("§cPortal masuk harus berada di dunia utama!");
                    return true;
                }

                portalManager.setPortalEntry(location);
                player.sendMessage("§aLokasi portal masuk berhasil disimpan!");
                break;

            case "setportalkeluar":
                if (!isDeathRealm(location)) {
                    player.sendMessage("§cPortal keluar harus berada di ALAMKACUNG!");
                    return true;
                }

                portalManager.setPortalExit(location);
                player.sendMessage("§aLokasi portal keluar berhasil disimpan!");
                break;

            default:
                player.sendMessage("§cCommand tidak dikenal.");
                break;
        }

        return true;
    }

    private boolean isDeathRealm(Location location) {
        return location.getWorld() != null
                && location.getWorld().getName().equalsIgnoreCase("DeathRealm");
    }
}
