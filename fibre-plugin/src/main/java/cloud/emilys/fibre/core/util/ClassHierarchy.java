package cloud.emilys.fibre.core.util;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ClassHierarchy {

    private ClassHierarchy() {
        throw new UnsupportedOperationException();
    }

    public static List<Class<?>> getAllClasses(Object object) {
        return getAllClasses(object.getClass());
    }

    public static List<Class<?>> getAllClasses(Class<?> type) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        visit(type, classes);
        return List.copyOf(classes);
    }

    private static void visit(Class<?> current, Set<Class<?>> classes) {
        if (!classes.add(current)) {
            return;
        }

        for (Class<?> anInterface : current.getInterfaces()) {
            visit(anInterface, classes);
        }

        Class<?> superclass = current.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return;
        }
        visit(superclass, classes);
    }
}
