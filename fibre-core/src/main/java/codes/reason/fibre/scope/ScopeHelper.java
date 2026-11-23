package codes.reason.fibre.scope;

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

    static <A extends Annotation> Class<A> queryable(Class<A> annotation, Class<? extends Annotation> kind) {
        Objects.requireNonNull(annotation, "annotation must not be null");
        if (!annotation.isAnnotationPresent(kind)) {
            throw new IllegalArgumentException(
                    "@%s is not @%s".formatted(annotation.getSimpleName(), kind.getSimpleName()));
        }
        return annotation;
    }

    static <V> void add(
            Map<Class<? extends Annotation>, List<V>> byAnnotation,
            V value,
            Class<? extends Annotation> kind,
            Class<? extends Annotation>[] annotations) {
        Objects.requireNonNull(annotations, "annotations must not be null");
        if (annotations.length == 0) {
            throw new IllegalArgumentException("%s must be grouped under at least one annotation".formatted(value));
        }
        for (Class<? extends Annotation> annotation : annotations) {
            queryable(annotation, kind);
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
