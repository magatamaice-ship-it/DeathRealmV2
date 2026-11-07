package id.deathrealm;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

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

            case "bangkit":
            case "intanhidup":
                if (args.length != 2) {
                    player.sendMessage(
                            "§cGunakan: /alamkacung intanhidup <nama_pemain>"
                    );
                    return true;
                }

                resurrectPlayer(player, args[1]);
                break;

            case "skip":
                if (args.length != 2) {
                    player.sendMessage(
                            "§cGunakan: /alamkacung skip <nama_pemain>"
                    );
                    return true;
                }

                skipFirstStage(player, args[1]);
                break;

            case "setpemanggilan":
                setSummoningAltar(player);
                break;

            case "setkelahiran":
                setRebirthAltar(player);
                break;

            case "setportalmasuk":
                setPortalEntry(player);
                break;

            case "setportalkeluar":
                setPortalExit(player);
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
                    player.sendMessage("§cJumlah harus berupa angka.");
                    return true;
                }

                if (amount <= 0) {
                    player.sendMessage("§cJumlah harus lebih dari 0.");
                    return true;
                }

                if (requirementManager.addRequirement(player, amount)) {
                    player.sendMessage(
                            "§aItem berhasil didaftarkan sebagai syarat ritual!"
                    );
                    player.sendMessage("§7Jumlah kebutuhan: §e" + amount);
                } else {
                    player.sendMessage(
                            "§cPegang item terlebih dahulu atau item sudah terdaftar."
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
                    player.sendMessage("§cNomor harus berupa angka.");
                    return true;
                }

                if (requirementManager.removeRequirement(index)) {
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
                List<DeathRealmFriendRevivalManager.Requirement>
                        requirements = requirementManager.getRequirements();

                player.sendMessage("§6§lPERSYARATAN RITUAL");

                if (requirements.isEmpty()) {
                    player.sendMessage("§7Belum ada persyaratan.");
                    return true;
                }

                for (int i = 0; i < requirements.size(); i++) {
                    DeathRealmFriendRevivalManager.Requirement requirement =
                            requirements.get(i);

                    ItemStack item = requirement.getItem();
                    ItemMeta meta = item.getItemMeta();

                    String itemName = item.getType().name();

                    if (meta != null && meta.hasDisplayName()) {
                        itemName = meta.getDisplayName();
                    }

                    player.sendMessage(
                            "§e" + (i + 1) + ". §f" + itemName
                    );
                    player.sendMessage(
                            "   §7Material: §f" + item.getType().name()
                    );
                    player.sendMessage(
                            "   §7Jumlah: §b" + requirement.getAmount()
                    );

                    if (meta != null && meta.hasLore()) {
                        player.sendMessage("   §7Lore:");

                        for (String loreLine : meta.getLore()) {
                            player.sendMessage("   §f- " + loreLine);
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
        private void setSummoningAltar(Player player) {
        Block block = player.getTargetBlockExact(10);

        if (block == null) {
            player.sendMessage(
                    "§cArahkan pandangan ke blok altar terlebih dahulu!"
            );
            return;
        }

        if (isDeathRealm(block.getLocation())) {
            player.sendMessage(
                    "§cAltar Pemanggilan harus berada di dunia utama!"
            );
            return;
        }

        altarManager.setSummoningAltar(block.getLocation());

        player.sendMessage(
                "§aLokasi Altar Pemanggilan berhasil disimpan!"
        );
        player.sendMessage(
                "§7Blok: §f" + block.getType().name()
        );
    }

    private void setRebirthAltar(Player player) {
        Block block = player.getTargetBlockExact(10);

        if (block == null) {
            player.sendMessage(
                    "§cArahkan pandangan ke blok altar terlebih dahulu!"
            );
            return;
        }

        if (!isDeathRealm(block.getLocation())) {
            player.sendMessage(
                    "§cAltar Kelahiran harus berada di ALAMKACUNG!"
            );
            return;
        }

        altarManager.setRebirthAltar(block.getLocation());

        player.sendMessage(
                "§aLokasi Altar Kelahiran berhasil disimpan!"
        );
        player.sendMessage(
                "§7Blok altar: §f" + block.getType().name()
        );
    }

    private void setPortalEntry(Player player) {
        Block block = player.getTargetBlockExact(10);

        if (block == null) {
            player.sendMessage(
                    "§cArahkan pandangan ke blok portal terlebih dahulu!"
            );
            return;
        }

        if (isDeathRealm(block.getLocation())) {
            player.sendMessage(
                    "§cPortal masuk harus berada di dunia utama!"
            );
            return;
        }

        portalManager.setPortalEntry(block.getLocation());

        player.sendMessage(
                "§aLokasi portal masuk berhasil disimpan!"
        );
        player.sendMessage(
                "§7Blok: §f" + block.getType().name()
        );
    }

    private void setPortalExit(Player player) {
        Block block = player.getTargetBlockExact(10);

        if (block == null) {
            player.sendMessage(
                    "§cArahkan pandangan ke blok portal terlebih dahulu!"
            );
            return;
        }

        if (!isDeathRealm(block.getLocation())) {
            player.sendMessage(
                    "§cPortal keluar harus berada di ALAMKACUNG!"
            );
            return;
        }

        portalManager.setPortalExit(block.getLocation());

        player.sendMessage(
                "§aLokasi portal keluar berhasil disimpan!"
        );
        player.sendMessage(
                "§7Blok: §f" + block.getType().name()
        );
    }

    private void resurrectPlayer(Player admin, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            admin.sendMessage(
                    "§cPemain harus online untuk menggunakan command ini."
            );
            return;
        }

        DeathRealm plugin =
                JavaPlugin.getPlugin(DeathRealm.class);

        if (!plugin.getTimerManager().hasTimer(target.getUniqueId())) {
            admin.sendMessage(
                    "§cPemain tersebut tidak sedang berada dalam status mati."
            );
            return;
        }

        World mainWorld = getMainWorld();

        if (mainWorld == null) {
            admin.sendMessage("§cDunia utama tidak ditemukan.");
            return;
        }

        Location destination = mainWorld.getSpawnLocation();

        if (!target.teleport(destination)) {
            admin.sendMessage("§cGagal memindahkan pemain.");
            return;
        }

        plugin.getSoulFragmentDisplay().removeFragments(
                target.getUniqueId()
        );

        plugin.getSoulFragmentManager().resetTrial(
                target.getUniqueId()
        );

        plugin.getTimerManager().removeTimer(
                target.getUniqueId()
        );

        target.setGameMode(GameMode.SURVIVAL);
        target.setFlying(false);
        target.setAllowFlight(false);
        target.setInvulnerable(false);
        target.setHealth(target.getMaxHealth());
        target.setFoodLevel(20);
        target.setSaturation(20);
        target.setFallDistance(0);
        target.setNoDamageTicks(0);

        admin.sendMessage(
                "§aBerhasil membangkitkan §e"
                        + target.getName() + "§a!"
        );

        target.sendMessage(
                "§aAdmin telah membangkitkanmu kembali ke dunia kehidupan!"
        );
    }
        private void skipFirstStage(Player admin, String targetName) {
        Player target = Bukkit.getPlayerExact(targetName);

        if (target == null) {
            admin.sendMessage(
                    "§cPemain harus online untuk menggunakan command ini."
            );
            return;
        }

        DeathRealm plugin =
                JavaPlugin.getPlugin(DeathRealm.class);

        if (!plugin.getTimerManager().hasTimer(target.getUniqueId())) {
            admin.sendMessage(
                    "§cPemain tersebut tidak sedang berada dalam status mati."
            );
            return;
        }

        if (!plugin.getTimerManager().forceUnlockTrial(
                target.getUniqueId()
        )) {
            admin.sendMessage(
                    "§cGagal membuka Tahap 2. Periksa status timer pemain."
            );
            return;
        }

        DeathRealmSoulFragmentManager fragmentManager =
                plugin.getSoulFragmentManager();

        if (!fragmentManager.isTrialActive(target.getUniqueId())) {
            boolean started = fragmentManager.startTrial(
                    target.getUniqueId()
            );

            if (!started) {
                admin.sendMessage(
                        "§cGagal memulai ujian Fragmen Jiwa."
                );
                return;
            }
        }

        plugin.getSoulFragmentDisplay().spawnFragments(
                target.getUniqueId(),
                target.getName()
        );

        admin.sendMessage(
                "§aTahap 1 berhasil dilewati untuk §e"
                        + target.getName() + "§a."
        );

        target.sendTitle(
                "§5TAHAP 2 TERBUKA",
                "§dTemukan tiga Fragmen Jiwa",
                10,
                60,
                20
        );

        target.sendMessage("§6§lTAHAP 1 DILEWATI!");
        target.sendMessage("§eAdmin telah membuka Tahap 2.");

        target.sendMessage(
                "§7Fragmen terkumpul: §f"
                        + fragmentManager.getCollectedCount(
                                target.getUniqueId()
                        ) + "/3"
        );

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (target.isOnline()) {
                plugin.getSoulFragmentMenu().openMenu(target);
            }
        });
    }

    private World getMainWorld() {
        for (World world : Bukkit.getWorlds()) {
            if (!world.getName().equalsIgnoreCase("DeathRealm")) {
                return world;
            }
        }

        return null;
    }

    private boolean isDeathRealm(Location location) {
        return location.getWorld() != null
                && location.getWorld().getName()
                .equalsIgnoreCase("DeathRealm");
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6§lPENGATURAN ALAMKACUNG");
        player.sendMessage("§e/alamkacung bangkit <nama_pemain>");
        player.sendMessage("§e/alamkacung intanhidup <nama_pemain>");
        player.sendMessage("§e/alamkacung skip <nama_pemain>");
        player.sendMessage("§e/alamkacung setpemanggilan");
        player.sendMessage("§e/alamkacung setkelahiran");
        player.sendMessage("§e/alamkacung setportalmasuk");
        player.sendMessage("§e/alamkacung setportalkeluar");
        player.sendMessage("§e/alamkacung setsyarat <jumlah>");
        player.sendMessage("§e/alamkacung hapus <nomor>");
        player.sendMessage("§e/alamkacung lihatsyarat");
    }
}
