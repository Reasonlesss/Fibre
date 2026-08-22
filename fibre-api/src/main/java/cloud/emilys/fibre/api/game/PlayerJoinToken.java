package cloud.emilys.fibre.api.game;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerJoinToken {

    Game getGame();

    UUID getPlayerId();

    void waitFor(CompletionStage<?> stage);

    void await();

    void join(Player player);

    void cancel();
}
