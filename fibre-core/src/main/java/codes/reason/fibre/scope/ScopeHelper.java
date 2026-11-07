package codes.reason.fibre.scope;

import codes.reason.fibre.annotations.Queryable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class ScopeHelper {

    private ScopeHelper() {}

    static <A extends Annotation> Class<A> queryable(Class<A> annotation) {
        Objects.requireNonNull(annotation, "annotation must not be null");
        if (!annotation.isAnnotationPresent(Queryable.class)) {
            throw new IllegalArgumentException(
                    "@%s is not @%s".formatted(annotation.getSimpleName(), Queryable.class.getSimpleName()));
        }
        return annotation;
    }

    static <V> void add(
            Map<Class<? extends Annotation>, List<V>> byAnnotation,
            V value,
            Class<? extends Annotation>[] annotations) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        if (annotations.length == 0) {
            throw new IllegalArgumentException("%s must be grouped under at least one annotation".formatted(value));
        }
        for (Class<? extends Annotation> annotation : annotations) {
            queryable(annotation);
        }
        for (Class<? extends Annotation> annotation : annotations) {
            List<V> values = byAnnotation.computeIfAbsent(annotation, _ -> new ArrayList<>());
            if (!values.contains(value)) {
                values.add(value);
            }
        }
    }

    static <K, V> Map<K, List<V>> copyLists(Map<K, List<V>> byKey) {
        Map<K, List<V>> copy = new HashMap<>();
        byKey.forEach((key, values) -> copy.put(key, List.copyOf(values)));
        return Map.copyOf(copy);
    }
}
