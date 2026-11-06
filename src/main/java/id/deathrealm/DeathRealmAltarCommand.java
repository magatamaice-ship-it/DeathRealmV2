
package id.deathrealm;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class DeathRealmAltarCommand implements CommandExecutor {

    private final DeathRealmAltarManager altarManager;
    private final DeathRealmPortalManager portalManager;
    private final DeathRealmFriendRevivalManager requirementManager;

    public DeathRealmAltarCommand(
            DeathRealmAltarManager altarManager,
            DeathRealmPortalManager portalManager,
            DeathRealmFriendRevivalManager requirementManager
    ) {
        this.altarManager = altarManager;
        this.portalManager = portalManager;
        this.requirementManager = requirementManager;
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(
                    "Perintah ini hanya bisa digunakan pemain."
            );
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

            case "setpemanggilan":

                if (isDeathRealm(player.getLocation())) {
                    player.sendMessage(
                            "§cAltar Pemanggilan harus berada di dunia utama!"
                    );
                    return true;
                }

                altarManager.setSummoningAltar(
                        player.getLocation()
                );

                player.sendMessage(
                        "§aLokasi Altar Pemanggilan berhasil disimpan!"
                );
                break;

            case "setkelahiran":

                if (!isDeathRealm(player.getLocation())) {
                    player.sendMessage(
                            "§cAltar Kelahiran harus berada di ALAMKACUNG!"
                    );
                    return true;
                }

                altarManager.setRebirthAltar(
                        player.getLocation()
                );

                player.sendMessage(
                        "§aLokasi Altar Kelahiran berhasil disimpan!"
                );
                break;

            case "setportalmasuk":

                if (isDeathRealm(player.getLocation())) {
                    player.sendMessage(
                            "§cPortal masuk harus berada di dunia utama!"
                    );
                    return true;
                }

                portalManager.setPortalEntry(
                        player.getLocation()
                );

                player.sendMessage(
                        "§aLokasi portal masuk berhasil disimpan!"
                );
                break;

            case "setportalkeluar":

                if (!isDeathRealm(player.getLocation())) {
                    player.sendMessage(
                            "§cPortal keluar harus berada di ALAMKACUNG!"
                    );
                    return true;
                }

                portalManager.setPortalExit(
                        player.getLocation()
                );

                player.sendMessage(
                        "§aLokasi portal keluar berhasil disimpan!"
                );
                break;

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
                    player.sendMessage(
                            "§cJumlah harus berupa angka."
                    );
                    return true;
                }

                if (amount <= 0) {
                    player.sendMessage(
                            "§cJumlah harus lebih dari 0."
                    );
                    return true;
                }

                if (requirementManager.addRequirement(
                        player,
                        amount
                )) {
                    player.sendMessage(
                            "§aItem berhasil didaftarkan sebagai syarat ritual!"
                    );

                    player.sendMessage(
                            "§7Jumlah kebutuhan: §e" + amount
                    );

                    player.sendMessage(
                            "§7Gunakan §e/alamkacung lihatsyarat §7untuk melihat daftar."
                    );

                } else {
                    player.sendMessage(
                            "§cPegang item terlebih dahulu atau item tersebut sudah terdaftar."
                    );
                }

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
                    player.sendMessage(
                            "§cNomor harus berupa angka."
                    );
                    return true;
                }

                if (requirementManager.removeRequirement(index)) {
                    player.sendMessage(
                            "§aPersyaratan nomor "
                                    + index
                                    + " berhasil dihapus."
                    );
                } else {
                    player.sendMessage(
                            "§cNomor persyaratan tidak ditemukan."
                    );
                }

                break;

            case "lihatsyarat":

                List<DeathRealmFriendRevivalManager.Requirement>
                        requirements =
                        requirementManager.getRequirements();

                player.sendMessage("§6§lPERSYARATAN RITUAL");

                if (requirements.isEmpty()) {
                    player.sendMessage(
                            "§7Belum ada persyaratan."
                    );
                    return true;
                }

                for (int i = 0; i < requirements.size(); i++) {

                    DeathRealmFriendRevivalManager.Requirement
                            requirement = requirements.get(i);

                    ItemStack item = requirement.getItem();

                    ItemMeta meta = item.getItemMeta();

                    String itemName = item.getType().name();

                    if (meta != null && meta.hasDisplayName()) {
                        itemName = meta.getDisplayName();
                    }

                    player.sendMessage(
                            "§e" + (i + 1)
                                    + ". §f" + itemName
                    );

                    player.sendMessage(
                            "   §7Material: §f"
                                    + item.getType().name()
                    );

                    player.sendMessage(
                            "   §7Jumlah: §b"
                                    + requirement.getAmount()
                    );

                    if (meta != null && meta.hasLore()) {
                        player.sendMessage("   §7Lore:");

                        for (String loreLine : meta.getLore()) {
                            player.sendMessage(
                                    "   §f- " + loreLine
                            );
                        }
                    }
                }

                break;

            default:
                sendHelp(player);
                break;
        }

        return true;
    }

    private boolean isDeathRealm(Location location) {
        return location.getWorld() != null
                && location.getWorld()
                .getName()
                .equalsIgnoreCase("DeathRealm");
    }

    private void sendHelp(Player player) {
        player.sendMessage(
                "§6§lPENGATURAN ALAMKACUNG"
        );

        player.sendMessage(
                "§e/alamkacung setpemanggilan"
        );

        player.sendMessage(
                "§e/alamkacung setkelahiran"
        );

        player.sendMessage(
                "§e/alamkacung setportalmasuk"
        );

        player.sendMessage(
                "§e/alamkacung setportalkeluar"
        );

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
