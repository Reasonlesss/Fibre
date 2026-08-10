package codes.reason.fibre.features.state;

import codes.reason.fibre.api.Fibre;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class StateFeature {

    private StateFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        fibre.getStartupRegistry().registerFactScanner(new StateFactScanner());
        fibre.getStartupRegistry().registerScopeContributor(new StateContributor());
    }
}
