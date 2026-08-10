package cloud.emilys.fibre.core.game;

import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;

@NullMarked
record GameCompletionListener<T>(Class<T> type, Consumer<T> consumer) {}
