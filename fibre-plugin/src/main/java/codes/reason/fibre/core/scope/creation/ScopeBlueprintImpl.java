package codes.reason.fibre.core.scope.creation;

import codes.reason.fibre.api.game.Game;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.Scope;
import codes.reason.fibre.api.scope.creation.ScopeBlueprint;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
record ScopeBlueprintImpl(Game game, @Nullable Scope parent, ObjectKey initialKey, Map<ObjectKey, Object> inputObjects)
        implements ScopeBlueprint {

    ScopeBlueprintImpl {
        Objects.requireNonNull(game, "game");
        Objects.requireNonNull(initialKey, "initialKey");
        inputObjects = Map.copyOf(Objects.requireNonNull(inputObjects, "inputObjects"));
        if (parent != null && parent.getGame() != game) {
            throw new IllegalArgumentException("Parent scope belongs to a different game");
        }
    }

    @Override
    public Game getGame() {
        return this.game;
    }

    @Override
    public @Nullable Scope getParent() {
        return this.parent;
    }

    @Override
    public ObjectKey getInitialKey() {
        return this.initialKey;
    }

    @Override
    public Map<ObjectKey, Object> getInputObjects() {
        return this.inputObjects;
    }
}
