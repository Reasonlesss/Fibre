package cloud.emilys.fibre.api.game;

import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlayerJoinToken {

    Game getGame();

    UUID getPlayerId();

    void waitFor(CompletionStage<?> stage);

    void cancel();
}
