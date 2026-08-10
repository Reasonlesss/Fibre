package cloud.emilys.fibre.core.type;

import cloud.emilys.fibre.api.type.TypeResolver;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class TypeResolverImpl implements TypeResolver {

    private final Map<LookupKey, RegisteredTypeFinder<?, ?>> finders;

    public TypeResolverImpl(List<RegisteredTypeFinder<?, ?>> finders) {
        Objects.requireNonNull(finders, "finders");
        Map<LookupKey, RegisteredTypeFinder<?, ?>> finderMap = new LinkedHashMap<>();
        for (RegisteredTypeFinder<?, ?> finder : finders) {
            finderMap.put(new LookupKey(finder.sourceType(), finder.targetType()), finder);
        }
        this.finders = Collections.unmodifiableMap(finderMap);
    }

    @Override
    public <T> Optional<T> find(Object source, Class<T> targetType) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(targetType, "targetType");

        if (targetType.isInstance(source)) {
            return Optional.of(targetType.cast(source));
        }

        LookupKey key = new LookupKey(source.getClass(), targetType);
        RegisteredTypeFinder<?, ?> exactFinder = this.finders.get(key);
        if (exactFinder != null) {
            return exactFinder.find(source).map(targetType::cast);
        }

        for (Map.Entry<LookupKey, RegisteredTypeFinder<?, ?>> entry : this.finders.entrySet()) {
            if (entry.getKey().sourceType().isInstance(source)
                    && entry.getKey().targetType().equals(targetType)) {
                return entry.getValue().find(source).map(targetType::cast);
            }
        }

        return Optional.empty();
    }

    @NullMarked
    private record LookupKey(Class<?> sourceType, Class<?> targetType) {

        private LookupKey {
            Objects.requireNonNull(sourceType, "sourceType");
            Objects.requireNonNull(targetType, "targetType");
        }
    }
}
