package codes.reason.fibre.core.scope.creation;

import codes.reason.fibre.api.PrimaryThreadUtil;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.Scope;
import codes.reason.fibre.api.scope.ScopeResolver;
import codes.reason.fibre.api.scope.binding.BindingResult;
import codes.reason.fibre.api.scope.creation.ScopeBlueprint;
import codes.reason.fibre.core.scope.ScopeImpl;
import codes.reason.fibre.core.scope.ScopedObjectImpl;
import codes.reason.fibre.core.scope.discovery.ScopeDefinition;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class AsyncScopeFactory
        extends AbstractScopeFactory<CompletionStage<Scope>, CompletionStage<Map<ObjectKey, ScopedObjectImpl>>> {

    private final Executor primaryExecutor =
            PrimaryThreadUtil.createExecutor(JavaPlugin.getProvidingPlugin(AsyncScopeFactory.class));

    @Override
    protected CompletionStage<Map<ObjectKey, ScopedObjectImpl>> begin(ScopeBlueprint blueprint) {
        return CompletableFuture.completedFuture(new LinkedHashMap<>());
    }

    @Override
    protected CompletionStage<Map<ObjectKey, ScopedObjectImpl>> append(
            ScopeBlueprint blueprint,
            CompletionStage<Map<ObjectKey, ScopedObjectImpl>> state,
            ScopeDefinition definition) {
        return state.thenComposeAsync(
                objects -> {
                    ScopeResolver resolver = key -> {
                        Objects.requireNonNull(key, "key");
                        ScopedObjectImpl object = objects.get(key);
                        if (object != null) {
                            return Optional.of(object);
                        }
                        Scope parent = blueprint.getParent();
                        return parent == null ? Optional.empty() : parent.get(key);
                    };
                    return this.resolve(resolver, definition)
                            .thenApplyAsync(
                                    value -> {
                                        materialize(objects, definition, value, resolver);
                                        return objects;
                                    },
                                    this.primaryExecutor)
                            .whenCompleteAsync(
                                    (_, failure) -> {
                                        if (failure != null) {
                                            cleanup(objects, failure);
                                        }
                                    },
                                    this.primaryExecutor);
                },
                this.primaryExecutor);
    }

    @Override
    protected CompletionStage<Scope> finish(
            ScopeBlueprint blueprint,
            CompletionStage<Map<ObjectKey, ScopedObjectImpl>> state,
            Consumer<Scope> consumer) {
        return state.thenApplyAsync(
                objects -> {
                    try {
                        Scope scope = new ScopeImpl(blueprint.getGame(), objects);
                        consumer.accept(scope);
                        return scope;
                    } catch (RuntimeException | Error failure) {
                        cleanup(objects, failure);
                        throw failure;
                    }
                },
                this.primaryExecutor);
    }

    private CompletionStage<Object> resolve(ScopeResolver resolver, ScopeDefinition definition) {
        BindingResult result = Objects.requireNonNull(
                definition.binding().make(resolver), "Binding returned null for %s".formatted(definition.key()));
        return switch (result) {
            case BindingResult.Immediate immediate -> CompletableFuture.completedFuture(immediate.value());
            case BindingResult.Deferred deferred ->
                Objects.requireNonNull(
                                deferred.stageSupplier().get(),
                                "Deferred binding returned null for %s".formatted(definition.key()))
                        .thenApply(value -> Objects.requireNonNull(
                                value, "Deferred binding completed with null for %s".formatted(definition.key())));
        };
    }
}
