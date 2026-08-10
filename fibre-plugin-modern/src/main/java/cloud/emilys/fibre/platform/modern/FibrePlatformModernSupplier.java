package cloud.emilys.fibre.platform.modern;

import cloud.emilys.fibre.platform.FibrePlatform;
import cloud.emilys.fibre.platform.FibrePlatformSupplier;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FibrePlatformModernSupplier implements FibrePlatformSupplier {

    private static final String PLATFORM_MARKER = "io.papermc.paper.connection.PlayerGameConnection";

    @Override
    public boolean isSupported() {
        try {
            Class.forName(PLATFORM_MARKER, false, this.getClass().getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError ignored) {
            return false;
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public FibrePlatform get() {
        return new FibrePlatformModern();
    }
}
