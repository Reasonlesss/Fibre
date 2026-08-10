package cloud.emilys.fibre.core;

import cloud.emilys.fibre.api.FibreBridge;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import cloud.emilys.fibre.core.scope.binding.DependencySetImpl;
import cloud.emilys.fibre.core.scope.creation.AbstractScopeFactory;
import cloud.emilys.fibre.core.scope.creation.ScopeBlueprintBuilderImpl;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FibreBridgeImpl implements FibreBridge {

    @Override
    public DependencySet createDependencySet() {
        return new DependencySetImpl();
    }

    @Override
    public ScopeBlueprint.Builder createScopeBlueprintBuilder() {
        return new ScopeBlueprintBuilderImpl();
    }

    @Override
    public ScopeFactory<Scope> createSynchronousScopeFactory() {
        return AbstractScopeFactory.synchronous();
    }

    @Override
    public ScopeFactory<CompletionStage<Scope>> createAsynchronousScopeFactory() {
        return AbstractScopeFactory.asynchronous();
    }
}
