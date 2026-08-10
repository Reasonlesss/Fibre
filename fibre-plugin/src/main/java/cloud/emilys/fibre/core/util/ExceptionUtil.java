package cloud.emilys.fibre.core.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ExceptionUtil {

    private ExceptionUtil() {
        throw new UnsupportedOperationException();
    }

    public static Throwable unwrap(Throwable exception) {
        Objects.requireNonNull(exception, "exception");

        Throwable current = exception;
        while (isWrapper(current) && current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }

    private static boolean isWrapper(Throwable exception) {
        return exception instanceof CompletionException
                || exception instanceof ExecutionException
                || exception instanceof InvocationTargetException;
    }
}
