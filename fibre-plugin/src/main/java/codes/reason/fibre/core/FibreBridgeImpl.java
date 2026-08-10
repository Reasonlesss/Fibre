package codes.reason.fibre.core;

import codes.reason.fibre.api.FibreBridge;
import codes.reason.fibre.api.scope.Scope;
import codes.reason.fibre.api.scope.binding.DependencySet;
import codes.reason.fibre.api.scope.creation.ScopeBlueprint;
import codes.reason.fibre.api.scope.creation.ScopeFactory;
import codes.reason.fibre.core.scope.binding.DependencySetImpl;
import codes.reason.fibre.core.scope.creation.AbstractScopeFactory;
import codes.reason.fibre.core.scope.creation.ScopeBlueprintBuilderImpl;
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
