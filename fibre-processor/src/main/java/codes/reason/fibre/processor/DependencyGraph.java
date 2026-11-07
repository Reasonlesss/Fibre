package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@NullMarked
final class DependencyGraph {

    private final Types types;
    private final Map<TypeElement, ManagedClass> classes = new LinkedHashMap<>();
    private final Map<TypeElement, ManagedClass> libraries = new HashMap<>();

    DependencyGraph(Types types, List<ManagedClass> classes, List<ManagedClass> libraries) {
        this.types = types;
        for (ManagedClass managed : classes) {
            this.classes.put(managed.type(), managed);
        }
        for (ManagedClass managed : libraries) {
            this.libraries.put(managed.type(), managed);
        }
    }

    @Nullable ManagedClass resolve(ManagedClass.Dependency dependency, ManagedClass root) {
        DeclaredType type = dependency.declaredType();
        ManagedClass managed = this.find((TypeElement) type.asElement());
        if (managed == null || !inScope(managed, root) || !Objects.equals(managed.name(), dependency.name())) {
            return null;
        }
        boolean parameterised = type.getTypeArguments().size() == managed.type().getTypeParameters().size()
                && type.getTypeArguments().stream().noneMatch(argument -> argument.getKind() == TypeKind.WILDCARD);
        return parameterised ? managed : null;
    }

    List<ManagedClass> implementations(DeclaredType type, ManagedClass root) {
        return this.classes.values().stream()
                .filter(managed -> inScope(managed, root) && !managed.isGeneric())
                .filter(managed -> this.types.isAssignable(managed.type().asType(), type))
                .toList();
    }

    List<ManagedClass> dependencies(ManagedClass managed, ManagedClass root) {
        return managed.dependencies()
                .filter(dependency -> !dependency.isIdentityOf(managed.type()))
                .flatMap(dependency -> dependency.isList()
                        ? this.implementations(dependency.requireListElement(), root).stream()
                        : Stream.<ManagedClass>ofNullable(this.resolve(dependency, root))
                ).toList();
    }

    private @Nullable ManagedClass find(TypeElement type) {
        ManagedClass managed = this.classes.get(type);
        return managed != null ? managed : this.libraries.get(type);
    }

    private static boolean inScope(ManagedClass managed, ManagedClass root) {
        return managed.kind() != ManagedClass.Kind.SCOPE || managed == root;
    }

}
