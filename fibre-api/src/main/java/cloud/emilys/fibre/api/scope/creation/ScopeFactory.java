package cloud.emilys.fibre.api.scope.creation;

import cloud.emilys.fibre.api.FibreBridge;
import cloud.emilys.fibre.api.scope.Scope;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopeFactory<T> {

    static ScopeFactory<Scope> synchronous() {
        return FibreBridge.get().createSynchronousScopeFactory();
    }

    static ScopeFactory<CompletionStage<Scope>> asynchronous() {
        return FibreBridge.get().createAsynchronousScopeFactory();
    }

    T initialize(ScopeBlueprint blueprint);
}
