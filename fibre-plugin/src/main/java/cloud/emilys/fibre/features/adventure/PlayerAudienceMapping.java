package cloud.emilys.fibre.features.adventure;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerAudienceMapping {

    Audience apply(Player player);
}
