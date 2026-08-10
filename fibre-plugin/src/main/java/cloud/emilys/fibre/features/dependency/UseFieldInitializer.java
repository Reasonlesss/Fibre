package cloud.emilys.fibre.features.dependency;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.ScopedObject;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import cloud.emilys.fibre.api.scope.lifecycle.ObjectInitializer;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class UseFieldInitializer implements ObjectInitializer {

    private final List<Field> fields;
    private final List<ObjectKey> dependencies;

    public UseFieldInitializer(List<Field> fields) {
        this.fields = List.copyOf(Objects.requireNonNull(fields, "fields"));
        this.dependencies = this.fields.stream()
                .map(Field::getAnnotatedType)
                .map(ObjectKey::fromAnnotatedType)
                .toList();
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create().add(this.dependencies);
    }

    @Override
    public void initialize(ScopeResolver resolver, ScopedObject object) {
        Objects.requireNonNull(resolver, "resolver");
        Objects.requireNonNull(object, "object");
        for (int index = 0; index < this.fields.size(); index++) {
            Field field = this.fields.get(index);
            ObjectKey dependency = this.dependencies.get(index);
            try {
                field.set(object.getObject(), resolver.require(dependency).getObject());
            } catch (IllegalAccessException exception) {
                throw new IllegalStateException(
                        "Cannot inject dependency into field %s".formatted(field.toGenericString()), exception);
            }
        }
    }
}
