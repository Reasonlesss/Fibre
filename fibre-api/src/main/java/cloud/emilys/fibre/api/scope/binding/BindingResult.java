package cloud.emilys.fibre.api.scope.binding;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@NullMarked
public sealed interface BindingResult {

    static BindingResult deferred(Supplier<CompletionStage<Object>> stageSupplier) {
        return new Deferred(stageSupplier);
    }

    static BindingResult immediate(Object value) {
        return new Immediate(value);
    }

    @NullMarked
    record Deferred(Supplier<CompletionStage<Object>> stageSupplier) implements BindingResult {

        public Deferred {
            Objects.requireNonNull(stageSupplier, "stageSupplier");
        }
    }

    @NullMarked
    record Immediate(Object value) implements BindingResult {

        public Immediate {
            Objects.requireNonNull(value, "value");
        }
    }
}
