package codes.reason.fibre;

import org.jspecify.annotations.NullMarked;

// anything registered with Fibre or the platform (listeners, tasks) that can be undone; undoing it twice does nothing
@NullMarked
public interface Registration extends AutoCloseable {

    void unregister();

    boolean isRegistered();

    @Override
    default void close() {
        this.unregister();
    }

}
