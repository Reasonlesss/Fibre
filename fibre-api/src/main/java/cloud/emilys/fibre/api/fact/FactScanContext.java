package cloud.emilys.fibre.api.fact;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface FactScanContext {

    Class<?> getType();

    List<Class<?>> getHierarchy();

    List<Method> getMethods();

    List<Field> getFields();

    List<Constructor<?>> getConstructors();

    <T> void put(FactKey<T> key, T value);

    void report(Object owner, String format, Object... args);

    boolean isValid();
}
