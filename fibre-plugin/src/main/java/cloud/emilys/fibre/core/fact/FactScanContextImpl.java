package cloud.emilys.fibre.core.fact;

import cloud.emilys.fibre.api.fact.FactKey;
import cloud.emilys.fibre.api.fact.FactScanContext;
import cloud.emilys.fibre.core.util.ClassHierarchy;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class FactScanContextImpl implements FactScanContext {

    private final Class<?> type;
    final Map<FactKey<?>, Object> data = new HashMap<>();
    final List<FactDiagnostic> diagnostics = new ArrayList<>();
    private final List<Class<?>> hierarchy;

    public FactScanContextImpl(Class<?> type) {
        Objects.requireNonNull(type, "type");
        this.type = type;
        this.hierarchy = ClassHierarchy.getAllClasses(type);
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    @Override
    public List<Class<?>> getHierarchy() {
        return this.hierarchy;
    }

    @Override
    public List<Method> getMethods() {
        List<Method> methods = new ArrayList<>();
        for (Class<?> aClass : hierarchy) {
            methods.addAll(List.of(aClass.getDeclaredMethods()));
        }
        return Collections.unmodifiableList(methods);
    }

    @Override
    public List<Field> getFields() {
        List<Field> fields = new ArrayList<>();
        for (Class<?> aClass : hierarchy) {
            fields.addAll(List.of(aClass.getDeclaredFields()));
        }
        return Collections.unmodifiableList(fields);
    }

    @Override
    public List<Constructor<?>> getConstructors() {
        return List.of(this.type.getDeclaredConstructors());
    }

    @Override
    public <T> void put(FactKey<T> key, T value) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");
        this.data.put(key, value);
    }

    @Override
    public void report(Object owner, String format, Object... args) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(format, "format");
        this.diagnostics.add(new FactDiagnostic(owner, format.formatted(args)));
    }

    @Override
    public boolean isValid() {
        return this.diagnostics.isEmpty();
    }
}
