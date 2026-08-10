package cloud.emilys.fibre.core.scope.creation;

import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ScopeBlueprintBuilderImpl implements ScopeBlueprint.Builder {

    private final Map<ObjectKey, Object> inputObjects = new LinkedHashMap<>();

    private @Nullable Game game;
    private @Nullable Scope parent;
    private @Nullable ObjectKey initialKey;

    public ScopeBlueprintBuilderImpl() {}

    @Override
    public ScopeBlueprint.Builder setGame(Game game) {
        this.game = Objects.requireNonNull(game, "game");
        return this;
    }

    @Override
    public ScopeBlueprint.Builder setParent(@Nullable Scope parent) {
        this.parent = parent;
        if (parent != null) {
            this.game = parent.getGame();
        }
        return this;
    }

    @Override
    public ScopeBlueprint.Builder setInitialKey(ObjectKey initialKey) {
        this.initialKey = Objects.requireNonNull(initialKey, "initialKey");
        return this;
    }

    @Override
    public ScopeBlueprint.Builder setInputObject(ObjectKey key, Object object) {
        this.inputObjects.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(object, "object"));
        return this;
    }

    @Override
    public ScopeBlueprint.Builder setInputObjects(Map<ObjectKey, Object> inputObjects) {
        Objects.requireNonNull(inputObjects, "inputObjects").forEach(this::setInputObject);
        return this;
    }

    @Override
    public ScopeBlueprint build() {
        return new ScopeBlueprintImpl(
                Objects.requireNonNull(this.game, "game"),
                this.parent,
                Objects.requireNonNull(this.initialKey, "initialKey"),
                this.inputObjects);
    }
}
