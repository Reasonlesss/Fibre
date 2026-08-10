package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.fact.FactKey;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DependencyFacts {

    public static FactKey<Constructor<?>> CONSTRUCTOR =
            FactKey.of("fibre", "dependency_implicit_constructor", Constructor.class);

    public static FactKey<List<Field>> USE_FIELDS = FactKey.of("fibre", "dependency_use_fields", List.class);

    public static FactKey<List<Field>> EXPOSE_FIELDS = FactKey.of("fibre", "dependency_expose_fields", List.class);

    public static FactKey<List<Method>> STATIC_PROVIDES_METHODS =
            FactKey.of("fibre", "dependency_static_provides_methods", List.class);

    public static FactKey<List<Method>> INSTANCE_PROVIDES_METHODS =
            FactKey.of("fibre", "dependency_instance_provides_methods", List.class);

    private DependencyFacts() {
        throw new UnsupportedOperationException();
    }
}
