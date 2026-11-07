package codes.reason.fibre.blueprint;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayDeque;
import java.util.Deque;

@NullMarked
final class ScopeBlueprints {

    static final ClassValue<ScopeBlueprint> BLUEPRINTS = new ClassValue<>() {
        @Override
        protected ScopeBlueprint computeValue(Class<?> type) {
            return load(type);
        }
    };

    private ScopeBlueprints() {
    }

    private static ScopeBlueprint load(Class<?> type) {
        String name = name(type);
        try {
            Class<?> blueprint = Class.forName(name, true, type.getClassLoader());
            return (ScopeBlueprint) blueprint.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("%s has no generated %s, so it isn't a @Game or @Scoped class"
                    .formatted(type.getName(), name), e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Couldn't create %s".formatted(name), e);
        }
    }

    private static String name(Class<?> type) {
        Deque<String> names = new ArrayDeque<>();
        for (Class<?> current = type; current != null; current = current.getEnclosingClass()) {
            names.addFirst(current.getSimpleName());
        }
        String packageName = type.getPackageName();
        String simpleName = String.join("_", names) + "_Blueprint";
        return packageName.isEmpty() ? simpleName : packageName + "." + simpleName;
    }

}
