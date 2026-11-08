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

    sealed interface Node permits ManagedClass, ManagedClass.Provider {

        String describe();
    }

    // only a scope object has providers, so a child scope requires what its parents provide from them
    ManagedClass.@Nullable Provider provider(ManagedClass.Dependency dependency, ManagedClass root) {
        return root.providers().stream()
                .filter(provider -> this.types.isSameType(provider.type(), dependency.identityType()))
                .filter(provider -> Objects.equals(provider.name(), dependency.identityName()))
                .findFirst()
                .orElse(null);
    }

    List<ManagedClass.Provider> providers(DeclaredType type, ManagedClass root) {
        return root.providers().stream()
                .filter(provider -> this.types.isAssignable(provider.type(), type))
                .toList();
    }

    @Nullable ManagedClass resolve(ManagedClass.Dependency dependency, ManagedClass root) {
        DeclaredType type = dependency.identityType();
        ManagedClass managed = this.find((TypeElement) type.asElement());
        if (managed == null || !inScope(managed, root) || !Objects.equals(managed.name(), dependency.identityName())) {
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

    List<Node> dependencies(Node node, ManagedClass root) {
        return dependencies(node).flatMap(dependency -> this.nodes(dependency, root)).toList();
    }

    // what the node's handles refer to, which must be built but not before the node
    List<Node> handled(Node node, ManagedClass root) {
        return dependencies(node)
                .filter(dependency -> dependency.isHandle() && !dependency.isScopeHandle())
                .flatMap(dependency -> this.target(dependency, root))
                .toList();
    }

    private static Stream<ManagedClass.Dependency> dependencies(Node node) {
        return switch (node) {
            case ManagedClass managed -> managed.dependencies().filter(dependency -> !dependency.isIdentityOf(managed.type()));
            case ManagedClass.Provider provider -> provider.dependencies().stream();
        };
    }

    private Stream<Node> nodes(ManagedClass.Dependency dependency, ManagedClass root) {
        if (dependency.isHandle()) {
            return Stream.empty();
        }
        if (dependency.isList()) {
            DeclaredType element = dependency.requireListElement();
            return Stream.concat(this.providers(element, root).stream(), this.implementations(element, root).stream());
        }
        return this.target(dependency, root);
    }

    private Stream<Node> target(ManagedClass.Dependency dependency, ManagedClass root) {
        ManagedClass.Provider provider = this.provider(dependency, root);
        return provider != null ? Stream.of(provider) : Stream.ofNullable(this.resolve(dependency, root));
    }

    private @Nullable ManagedClass find(TypeElement type) {
        ManagedClass managed = this.classes.get(type);
        return managed != null ? managed : this.libraries.get(type);
    }

    private static boolean inScope(ManagedClass managed, ManagedClass root) {
        return managed.kind() != ManagedClass.Kind.SCOPE || managed == root;
    }

}
