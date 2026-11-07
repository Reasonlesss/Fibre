package codes.reason.fibre.plugin;

import codes.reason.fibre.scope.ScopeInject;
import codes.reason.fibre.scope.ScopeSetup;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopePlugin {

    default void inject(ScopeInject inject) {}

    default void setup(ScopeSetup setup) {}
}
