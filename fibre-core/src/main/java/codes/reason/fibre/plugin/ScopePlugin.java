package codes.reason.fibre.plugin;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.scope.ScopeInject;
import codes.reason.fibre.scope.ScopeSetup;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ScopePlugin {

    default void inject(Fibre fibre, ScopeInject inject) {}

    default void setup(Fibre fibre, ScopeSetup setup) {}
}
