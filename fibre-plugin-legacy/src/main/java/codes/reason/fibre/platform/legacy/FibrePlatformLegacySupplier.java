package codes.reason.fibre.platform.legacy;

import codes.reason.fibre.platform.FibrePlatform;
import codes.reason.fibre.platform.FibrePlatformSupplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FibrePlatformLegacySupplier implements FibrePlatformSupplier {

    @Override
    public FibrePlatform get() {
        return new FibrePlatformLegacy();
    }
}
