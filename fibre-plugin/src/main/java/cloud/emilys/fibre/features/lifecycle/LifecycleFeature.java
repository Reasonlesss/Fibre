package cloud.emilys.fibre.features.lifecycle;

import cloud.emilys.fibre.api.Fibre;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class LifecycleFeature {

    private LifecycleFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        fibre.getStartupRegistry().registerFactScanner(new LifecycleFactScanner());
        fibre.getStartupRegistry().registerScopeContributor(new LifecycleContributor());
    }
}
