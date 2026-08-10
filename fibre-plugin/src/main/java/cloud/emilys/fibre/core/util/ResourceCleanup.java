package cloud.emilys.fibre.core.util;

import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ResourceCleanup {

    private ResourceCleanup() {
        throw new UnsupportedOperationException();
    }

    public static void closeAll(List<? extends AutoCloseable> closeables) throws Exception {
        Exception failure = null;
        for (int i = closeables.size() - 1; i >= 0; i--) {
            try {
                closeables.get(i).close();
            } catch (Exception exception) {
                if (failure == null) {
                    failure = exception;
                } else {
                    failure.addSuppressed(exception);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    public static void closeAll(List<? extends AutoCloseable> closeables, Throwable failure) {
        for (int i = closeables.size() - 1; i >= 0; i--) {
            try {
                closeables.get(i).close();
            } catch (Throwable closeFailure) {
                failure.addSuppressed(closeFailure);
            }
        }
    }

    public static void closeAllQuietly(List<? extends AutoCloseable> closeables) {
        for (int i = closeables.size() - 1; i >= 0; i--) {
            try {
                closeables.get(i).close();
            } catch (Exception _) {
            }
        }
    }
}
