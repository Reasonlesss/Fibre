package cloud.emilys.fibre.api.scope.creation;

import cloud.emilys.fibre.api.FibreBridge;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import java.lang.reflect.Type;
import java.util.Map;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ScopeBlueprint {

    static Builder builder() {
        return FibreBridge.get().createScopeBlueprintBuilder();
    }

    Game getGame();

    @Nullable
    Scope getParent();

    ObjectKey getInitialKey();

    Map<ObjectKey, Object> getInputObjects();

    @NullMarked
    interface Builder {

        Builder setGame(Game game);

        Builder setParent(@Nullable Scope parent);

        Builder setInitialKey(ObjectKey initialKey);

        Builder setInputObject(ObjectKey key, Object object);

        default Builder setInputObject(Type type, Object object) {
            return this.setInputObject(ObjectKey.fromType(type), object);
        }

        Builder setInputObjects(Map<ObjectKey, Object> inputObjects);

        ScopeBlueprint build();
    }
}
