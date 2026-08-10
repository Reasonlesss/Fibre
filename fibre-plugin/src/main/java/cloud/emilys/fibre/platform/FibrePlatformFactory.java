package cloud.emilys.fibre.platform;

import java.util.Comparator;
import java.util.ServiceLoader;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FibrePlatformFactory {

    private FibrePlatformFactory() {
        throw new UnsupportedOperationException();
    }

    public static FibrePlatform create() {
        return ServiceLoader.load(FibrePlatformSupplier.class, FibrePlatformFactory.class.getClassLoader()).stream()
                .map(ServiceLoader.Provider::get)
                .filter(FibrePlatformSupplier::isSupported)
                .max(Comparator.comparingInt(FibrePlatformSupplier::getPriority))
                .map(FibrePlatformSupplier::get)
                .orElseThrow(() -> new IllegalStateException("Could not find an appropriate fibre platform"));
    }
}
