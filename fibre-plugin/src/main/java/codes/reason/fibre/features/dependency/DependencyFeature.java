package codes.reason.fibre.features.dependency;

import codes.reason.fibre.api.Fibre;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyFeature {

    private DependencyFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        fibre.getStartupRegistry().registerFactScanner(new DependencyFactScanner());
        fibre.getStartupRegistry().registerScopeContributor(new DependencyContributor());
    }
}
