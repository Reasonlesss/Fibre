package cloud.emilys.fibre.core.scope.creation;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.core.scope.ScopeImpl;
import cloud.emilys.fibre.core.scope.ScopedObjectImpl;
import cloud.emilys.fibre.core.scope.discovery.ScopeDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class SyncScopeFactory extends AbstractScopeFactory<Scope, SyncScopeFactory.State> {

    @Override
    protected State begin(ScopeBlueprint blueprint) {
        return new State(blueprint);
    }

    @Override
    protected State append(ScopeBlueprint blueprint, State state, ScopeDefinition definition) {
        try {
            BindingResult result = Objects.requireNonNull(
                    definition.binding().make(state), "Binding returned null for %s".formatted(definition.key()));
            switch (result) {
                case BindingResult.Deferred _ ->
                    throw new IllegalStateException(
                            "Object %s cannot be instantiated from a synchronous context".formatted(definition.key()));
                case BindingResult.Immediate immediate -> state.materialize(definition, immediate.value());
            }
            return state;
        } catch (RuntimeException | Error failure) {
            state.cleanup(failure);
            throw failure;
        }
    }

    @Override
    protected Scope finish(ScopeBlueprint blueprint, State state, Consumer<Scope> consumer) {
        try {
            Scope scope = state.createScope();
            consumer.accept(scope);
            return scope;
        } catch (RuntimeException | Error failure) {
            state.cleanup(failure);
            throw failure;
        }
    }

    @NullMarked
    static final class State implements ScopeResolver {

        private final ScopeBlueprint blueprint;
        private final Map<ObjectKey, ScopedObjectImpl> objects = new LinkedHashMap<>();

        private State(ScopeBlueprint blueprint) {
            this.blueprint = Objects.requireNonNull(blueprint, "blueprint");
        }

        private void materialize(ScopeDefinition definition, Object value) {
            AbstractScopeFactory.materialize(this.objects, definition, value, this);
        }

        private Scope createScope() {
            return new ScopeImpl(this.blueprint.getGame(), this.objects);
        }

        private void cleanup(Throwable failure) {
            AbstractScopeFactory.cleanup(this.objects, failure);
        }

        @Override
        public Optional<ScopedObject> get(ObjectKey key) {
            Objects.requireNonNull(key, "key");
            ScopedObject object = this.objects.get(key);
            if (object != null) {
                return Optional.of(object);
            }
            Scope parent = this.blueprint.getParent();
            return parent == null ? Optional.empty() : parent.get(key);
        }
    }
}
