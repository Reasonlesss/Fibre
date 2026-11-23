package codes.reason.fibre.game;

import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

@NullMarked
record GameResultListener<T>(Class<T> type, Consumer<? super T> listener) {

    void invoke(Object object) {
        if (type.isInstance(object)) {
            listener.accept(type.cast(object));
        }
    }

}
