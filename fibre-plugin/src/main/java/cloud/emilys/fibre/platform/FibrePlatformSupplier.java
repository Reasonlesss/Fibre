package cloud.emilys.fibre.platform;

import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FibrePlatformSupplier extends Supplier<FibrePlatform> {

    default boolean isSupported() {
        return true;
    }

    default int getPriority() {
        return 0;
    }
}
