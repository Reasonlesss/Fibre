package cloud.emilys.fibre.core.fact;

import cloud.emilys.fibre.api.fact.FactKey;
import cloud.emilys.fibre.api.fact.Facts;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FactsImpl implements Facts {

    private final Map<FactKey<?>, Object> data;

    FactsImpl(Map<FactKey<?>, Object> data) {
        this.data = Map.copyOf(data);
    }

    @Override
    public <T> Optional<T> get(FactKey<T> key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(key.cast(this.data.get(key)));
    }
}
