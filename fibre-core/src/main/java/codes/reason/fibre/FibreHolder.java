package codes.reason.fibre;

import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FibreHolder {

    private static @Nullable Fibre instance;

    private FibreHolder() {}

    public static Fibre get() {
        Fibre fibre = instance;
        if (fibre == null) {
            throw new IllegalStateException("Fibre hasn't been enabled yet");
        }
        return fibre;
    }

    public static void set(Fibre fibre) {
        Objects.requireNonNull(fibre, "fibre must not be null");
        if (instance != null) {
            throw new IllegalStateException("Fibre has already been enabled");
        }
        instance = fibre;
    }

    public static void clear() {
        instance = null;
    }

}
