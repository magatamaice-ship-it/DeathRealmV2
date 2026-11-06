package id.deathrealm;

import org.bukkit.entity.Player;

public class DeathRealmMessages {

    private final DeathRealm plugin;

    public DeathRealmMessages(DeathRealm plugin) {
        this.plugin = plugin;
    }

    public void sendEntryMessage(Player player) {
        player.sendTitle(
                "§5ALAMKACUNG",
                "§7Jiwamu telah meninggalkan dunia kehidupan",
                10,
                60,
                20
        );

        player.sendMessage(
                "§8[§5ALAMKACUNG§8] §7Kamu memiliki waktu 25 menit sebelum dibangkitkan."
        );
    }

    public void sendResurrectionMessage(Player player) {
        player.sendTitle(
                "§aKEMBALI HIDUP",
                "§7Jiwamu telah kembali ke dunia kehidupan",
                10,
                60,
                20
        );

        player.sendMessage(
                "§8[§aALAMKACUNG§8] §aKamu berhasil dibangkitkan!"
        );
    }

    public void sendAlmostExpiredMessage(Player player) {
        player.sendMessage(
                "§8[§cALAMKACUNG§8] §cWaktumu hampir habis!"
        );
    }
}
