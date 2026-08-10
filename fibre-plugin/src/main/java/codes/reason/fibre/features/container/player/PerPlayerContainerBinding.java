package codes.reason.fibre.features.container.player;

import codes.reason.fibre.api.game.Players;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.ScopeResolver;
import codes.reason.fibre.api.scope.ScopedObject;
import codes.reason.fibre.api.scope.binding.Binding;
import codes.reason.fibre.api.scope.binding.BindingResult;
import codes.reason.fibre.api.scope.binding.DependencySet;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PerPlayerContainerBinding implements Binding {

    private static final ObjectKey PLAYERS = ObjectKey.fromType(Players.class);

    private final ObjectKey valueKey;

    public PerPlayerContainerBinding(ObjectKey containerKey) {
        Type containerType = containerKey.type();
        if (!(containerType instanceof ParameterizedType parameterized)
                || parameterized.getActualTypeArguments().length != 1) {
            throw new IllegalArgumentException(
                    "Per-player containers must declare one value type: %s".formatted(containerKey));
        }
        this.valueKey = ObjectKey.fromType(parameterized.getActualTypeArguments()[0]);
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create().add(PLAYERS);
    }

    @Override
    public BindingResult make(ScopeResolver resolver) {
        @SuppressWarnings("resource")
        Players players = (Players) resolver.require(PLAYERS).getObject();
        return BindingResult.immediate(new PerPlayerContainer<>(this.valueKey, players));
    }

    @Override
    public void destroy(ScopeResolver resolver, ScopedObject object) {
        if (object.getObject() instanceof PerPlayerContainer<?> container) {
            container.close();
        }
    }
}
