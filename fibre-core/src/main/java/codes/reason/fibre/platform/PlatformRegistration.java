package codes.reason.fibre.platform;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface PlatformRegistration extends AutoCloseable {

    void unregister();

    boolean isRegistered();

    @Override
    default void close() {
        this.unregister();
    }

}
