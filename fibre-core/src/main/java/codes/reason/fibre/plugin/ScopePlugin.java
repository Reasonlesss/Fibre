package codes.reason.fibre.plugin;

import codes.reason.fibre.scope.ScopeSetup;
import org.jspecify.annotations.NullMarked;

@NullMarked
@FunctionalInterface
public interface ScopePlugin {

    void apply(ScopeSetup setup);
}
