package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.Fibre;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadFeature {

    private PlayerPreloadFeature() {
        throw new UnsupportedOperationException();
    }

    public static void install(Fibre fibre) {
        Objects.requireNonNull(fibre, "fibre");
        fibre.getStartupRegistry().registerFactScanner(new PlayerPreloadFactScanner());
        fibre.getStartupRegistry().registerScopeContributor(new PlayerPreloadContributor());
        fibre.getStartupRegistry().registerScopeInitializer(new PlayerPreloadInitializer());
        fibre.getStartupRegistry().registerPlayerPreloader(new PlayerPreloadInvoker());
    }
}
