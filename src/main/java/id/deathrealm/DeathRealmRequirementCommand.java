
package id.deathrealm;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class DeathRealmRequirementCommand implements CommandExecutor {

    private final DeathRealmFriendRevivalManager manager;

    public DeathRealmRequirementCommand(
            DeathRealmFriendRevivalManager manager
    ) {
        this.manager = manager;
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

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "setsyarat":
                if (args.length != 2) {
                    player.sendMessage(
                            "§cGunakan: /alamkacung setsyarat <jumlah>"
                    );
                    return true;
                }

                int amount;

                try {
                    amount = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage("§cJumlah harus berupa angka.");
                    return true;
                }

                if (amount <= 0) {
                    player.sendMessage("§cJumlah harus lebih dari 0.");
                    return true;
                }

                if (manager.addRequirement(player, amount)) {
                    player.sendMessage(
                            "§aItem di tangan berhasil didaftarkan sebagai syarat ritual!"
                    );
                    player.sendMessage(
                            "§7Jumlah yang dibutuhkan: §e" + amount
                    );
                } else {
                    player.sendMessage(
                            "§cGagal. Pegang item terlebih dahulu atau item tersebut sudah terdaftar."
                    );
                }
                break;

            case "hapussy arat":
                player.sendMessage(
                        "§cGunakan: /alamkacung hapussy arat <nomor>"
                );
                break;

            case "hapus":
                if (args.length != 2) {
                    player.sendMessage(
                            "§cGunakan: /alamkacung hapus <nomor>"
                    );
                    return true;
                }

                int index;

                try {
                    index = Integer.parseInt(args[1]);
                } catch (NumberFormatException exception) {
                    player.sendMessage("§cNomor harus berupa angka.");
                    return true;
                }

                if (manager.removeRequirement(index)) {
                    player.sendMessage(
                            "§aPersyaratan nomor " + index
                                    + " berhasil dihapus."
                    );
                } else {
                    player.sendMessage(
                            "§cNomor persyaratan tidak ditemukan."
                    );
                }
                break;

            case "lihatsyarat":
                List<DeathRealmFriendRevivalManager.Requirement> requirements =
                        manager.getRequirements();

                player.sendMessage("§6§lPERSYARATAN RITUAL");

                if (requirements.isEmpty()) {
                    player.sendMessage("§7Belum ada persyaratan.");
                    return true;
                }

                for (int i = 0; i < requirements.size(); i++) {
                    DeathRealmFriendRevivalManager.Requirement requirement =
                            requirements.get(i);

                    player.sendMessage(
                            "§e" + (i + 1) + ". §f"
                                    + requirement.getItem().getType()
                                    + " §7x §b"
                                    + requirement.getAmount()
                    );
                }
                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lPENGATURAN SYARAT RITUAL");
        player.sendMessage(
                "§e/alamkacung setsyarat <jumlah>"
        );
        player.sendMessage(
                "§e/alamkacung hapus <nomor>"
        );
        player.sendMessage(
                "§e/alamkacung lihatsyarat"
        );
    }
}
