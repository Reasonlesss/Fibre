package codes.reason.fibre.platform.legacy;

import codes.reason.fibre.api.Fibre;
import codes.reason.fibre.features.adventure.AdventureFeature;
import net.kyori.adventure.audience.Audience;
import org.bukkit.command.CommandSender;

public final class FibreAdventureLegacy {

    private FibreAdventureLegacy() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        // Adventure support is only available on 1.8 forks that include it natively.
        if (Audience.class.isAssignableFrom(CommandSender.class)) {
            AdventureFeature.install(fibre, player -> (Audience) player);
        }
    }
}
