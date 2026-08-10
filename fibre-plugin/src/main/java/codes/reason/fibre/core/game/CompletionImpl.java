package codes.reason.fibre.core.game;

import codes.reason.fibre.api.game.Completion;
import codes.reason.fibre.api.game.Game;
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
