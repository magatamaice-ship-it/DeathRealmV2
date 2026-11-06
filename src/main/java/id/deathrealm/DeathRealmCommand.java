
package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeathRealmCommand implements CommandExecutor {

    private final DeathRealm plugin;
    private final DeathRealmResurrection resurrection;

    public DeathRealmCommand(
            DeathRealm plugin,
            DeathRealmResurrection resurrection
    ) {
        this.plugin = plugin;
        this.resurrection = resurrection;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!sender.hasPermission("deathrealm.admin")) {
            sender.sendMessage("§cKamu tidak memiliki izin!");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(
                    "§cGunakan: /lahirkankembali <nama_player>"
            );
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage(
                    "§cPemain tersebut tidak ditemukan atau sedang offline."
            );
            return true;
        }

        if (!plugin.getTimerManager().hasTimer(target.getUniqueId())) {
            sender.sendMessage(
                    "§ePemain tersebut tidak sedang berada dalam masa kematian."
            );
            return true;
        }

        boolean started = resurrection.resurrect(target, success -> {
            if (success) {
                sender.sendMessage(
                        "§aBerhasil membangkitkan §f"
                                + target.getName() + "§a!"
                );
            } else {
                sender.sendMessage(
                        "§cKebangkitan §f"
                                + target.getName()
                                + " §cgagal atau dibatalkan."
                );
            }
        });

        if (!started) {
            sender.sendMessage(
                    "§cProses kebangkitan tidak dapat dimulai."
            );
        }

        return true;
    }
}
