package codes.reason.fibre.features.container;

import codes.reason.fibre.api.Fibre;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ContainerFeature {

    private ContainerFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        fibre.getStartupRegistry().registerScopeContributor(new ContainerContributor());
    }
}
