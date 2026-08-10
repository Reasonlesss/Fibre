package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.game.Completion;
import cloud.emilys.fibre.api.game.Game;
import java.util.Objects;

public final class CompletionImpl implements Completion {

    private final Game game;

    public CompletionImpl(Game game) {
        this.game = game;
    }

    @Override
    public void complete(Object result) {
        Objects.requireNonNull(result, "result");
        this.game.complete(result);
    }
}
