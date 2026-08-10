package cloud.emilys.fibre.platform.legacy;

import cloud.emilys.fibre.platform.FibrePlatform;
import cloud.emilys.fibre.platform.FibrePlatformSupplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FibrePlatformLegacySupplier implements FibrePlatformSupplier {

    @Override
    public FibrePlatform get() {
        return new FibrePlatformLegacy();
    }
}
