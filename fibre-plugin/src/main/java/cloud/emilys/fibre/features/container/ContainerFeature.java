package cloud.emilys.fibre.features.container;

import cloud.emilys.fibre.api.Fibre;
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
