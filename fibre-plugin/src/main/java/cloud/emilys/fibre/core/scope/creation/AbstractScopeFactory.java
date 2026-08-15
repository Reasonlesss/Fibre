package cloud.emilys.fibre.core.scope.creation;

import cloud.emilys.fibre.api.Fibre;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import cloud.emilys.fibre.api.scope.lifecycle.ScopeInitializer;
import cloud.emilys.fibre.core.FibreImpl;
import cloud.emilys.fibre.core.scope.ScopedObjectImpl;
import cloud.emilys.fibre.core.scope.binding.DependencyGraph;
import cloud.emilys.fibre.core.scope.binding.StaticBinding;
import cloud.emilys.fibre.core.scope.discovery.ScopeCollectorImpl;
import cloud.emilys.fibre.core.scope.discovery.ScopeDefinition;
import cloud.emilys.fibre.core.scope.discovery.ScopeDefinitions;
import cloud.emilys.fibre.core.util.ResourceCleanup;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class AbstractScopeFactory<T, S> implements ScopeFactory<T> {

    public static ScopeFactory<Scope> synchronous() {
        return new SyncScopeFactory();
    }

    public static ScopeFactory<CompletionStage<Scope>> asynchronous() {
        return new AsyncScopeFactory();
    }

    @Override
    public final T initialize(ScopeBlueprint blueprint) {
        ScopeCollectorImpl collector = new ScopeCollectorImpl();

        blueprint
                .getInputObjects()
                .forEach((objectKey, object) -> collector.bind(objectKey, new StaticBinding(object)));
        collector.visit(blueprint.getInitialKey());

        ScopeDefinitions definitions = collector.collect();
        DependencyGraph graph = new DependencyGraph();
        for (ScopeDefinition definition : definitions) {
            graph.addDependencies(definition.key(), definition.binding().getDependencies());
            for (ObjectInitializer initializer : definition.initializers()) {
                graph.addDependencies(definition.key(), initializer.getDependencies());
            }
            for (ObjectInitializer initializer : definition.activators()) {
                graph.addDependencies(definition.key(), initializer.getDependencies());
            }
            for (ObjectInitializer initializer : definition.postInitializers()) {
                graph.addDependencies(definition.key(), initializer.getDependencies());
            }
        }

        List<ObjectKey> keys = graph.sort();
        List<ScopeDefinition> localDefinitions = new ArrayList<>();
        S state = this.begin(blueprint);
        for (ObjectKey key : keys) {
            if (isProvidedByParent(blueprint, key)) {
                continue;
            }
            ScopeDefinition definition = definitions.require(key);
            localDefinitions.add(definition);
            state = this.append(blueprint, state, definition);
        }
        return this.finish(blueprint, state, scope -> {
            Scope parent = blueprint.getParent();
            if (parent != null) {
                scope.bindParent(parent);
            }
            for (ScopedObject object : scope.getLocalObjects()) {
                ((ScopedObjectImpl) object).bindTo(scope);
            }
            for (ScopeDefinition definition : localDefinitions) {
                ScopedObject object = scope.require(definition.key());
                for (ObjectInitializer initializer : definition.initializers()) {
                    initializer.initialize(scope, object);
                }
            }
            for (ScopeDefinition definition : localDefinitions) {
                ScopedObject object = scope.require(definition.key());
                for (ObjectInitializer initializer : definition.activators()) {
                    initializer.initialize(scope, object);
                }
            }
            for (ScopeDefinition definition : localDefinitions) {
                ScopedObject object = scope.require(definition.key());
                for (ObjectInitializer initializer : definition.postInitializers()) {
                    initializer.initialize(scope, object);
                }
            }
            for (ScopeInitializer initializer : ((FibreImpl) Fibre.get()).getScopeInitializers()) {
                initializer.initialize(scope);
            }
        });
    }

    private static boolean isProvidedByParent(ScopeBlueprint blueprint, ObjectKey key) {
        if (key.equals(blueprint.getInitialKey()) || blueprint.getInputObjects().containsKey(key)) {
            return false;
        }
        Scope parent = blueprint.getParent();
        return parent != null && parent.get(key).isPresent();
    }

    protected abstract S begin(ScopeBlueprint blueprint);

    protected abstract S append(ScopeBlueprint blueprint, S state, ScopeDefinition definition);

    protected abstract T finish(ScopeBlueprint blueprint, S state, Consumer<Scope> consumer);

    protected static void materialize(
            Map<ObjectKey, ScopedObjectImpl> objects,
            ScopeDefinition definition,
            Object value,
            ScopeResolver resolver) {
        Objects.requireNonNull(value, "value");
        ScopedObjectImpl object = new ScopedObjectImpl(value, definition.facts());
        object.track(() -> definition.binding().destroy(resolver, object));
        objects.put(definition.key(), object);
    }

    protected static void cleanup(Map<ObjectKey, ScopedObjectImpl> objects, Throwable failure) {
        ResourceCleanup.closeAll(List.copyOf(objects.values()), failure);
    }
}
