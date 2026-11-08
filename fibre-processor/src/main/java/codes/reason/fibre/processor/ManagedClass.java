package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@NullMarked
record ManagedClass(
        TypeElement type,
        List<Kind> kinds,
        List<ExecutableElement> useConstructors,
        @Nullable ExecutableElement noArgsConstructor,
        List<Injection> injections,
        List<ExecutableElement> enterMethods,
        List<ExecutableElement> exitMethods,
        List<Queried> queried,
        List<Provider> providers,
        @Nullable TypeElement options
) implements DependencyGraph.Node {

    static boolean isOptions(TypeElement type) {
        Set<Modifier> modifiers = type.getModifiers();
        return type.getKind() == ElementKind.CLASS && modifiers.contains(Modifier.ABSTRACT) && modifiers.contains(Modifier.SEALED)
                && Annotations.inherits(type, Annotations.SCOPED);
    }

    @Nullable String name() {
        return Annotations.name(this.type);
    }

    Kind kind() {
        return this.kinds.getFirst();
    }

    boolean isBase() {
        return this.type.getKind() == ElementKind.CLASS && this.type.getModifiers().contains(Modifier.ABSTRACT);
    }

    @Nullable ExecutableElement constructor() {
        if (this.isBase()) {
            return null;
        }
        return this.useConstructors.isEmpty() ? this.noArgsConstructor : this.useConstructors.getFirst();
    }

    boolean isGeneric() {
        return !this.type.getTypeParameters().isEmpty();
    }

    List<Dependency> constructorDependencies() {
        ExecutableElement constructor = this.constructor();
        return constructor != null ? Dependency.of(constructor) : List.of();
    }

    Stream<Dependency> dependencies() {
        return Stream.concat(
                this.constructorDependencies().stream(),
                this.injections.stream().flatMap(injection -> injection.dependencies().stream())
        );
    }

    List<Dependency> suppliedDependencies() {
        return this.dependencies().filter(dependency -> !dependency.isIdentityOf(this.type)).toList();
    }

    Stream<Dependency> providerDependencies() {
        return this.providers.stream().flatMap(provider -> provider.dependencies().stream());
    }

    Stream<Element> usedMembers() {
        return Stream.of(
                Stream.<Element>ofNullable(this.constructor()),
                this.injections.stream().map(Injection::member),
                this.enterMethods.stream(),
                this.exitMethods.stream(),
                this.queried.stream().map(Queried::member),
                this.providers.stream().map(Provider::method)
        ).flatMap(stream -> stream);
    }

    @Override
    public String describe() {
        return this.type.getSimpleName().toString();
    }

    enum Kind {
        MANAGED,
        MANAGED_MANY,
        SCOPE
    }

    record Injection(Element member) {

        boolean isField() {
            return this.member.getKind() == ElementKind.FIELD;
        }

        List<Dependency> dependencies() {
            return switch (this.member) {
                case VariableElement field -> List.of(new Dependency(field));
                case ExecutableElement method -> Dependency.of(method);
                default -> List.of();
            };
        }
    }

    record Queried(Element member, List<TypeElement> annotations) {
    }

    record Provider(ExecutableElement method) implements DependencyGraph.Node {

        List<Dependency> dependencies() {
            return Dependency.of(this.method);
        }

        TypeMirror type() {
            return this.method.getReturnType();
        }

        DeclaredType declaredType() {
            return (DeclaredType) this.type();
        }

        @Nullable String name() {
            return Annotations.name(this.type());
        }

        @Override
        public String describe() {
            return "%s.%s()".formatted(this.method.getEnclosingElement().getSimpleName(), this.method.getSimpleName());
        }
    }

    record Dependency(VariableElement element) {

        private static final String LIST = "java.util.List";
        private static final String IDENTITY = "codes.reason.fibre.identity.Identity";
        private static final String HANDLE = "codes.reason.fibre.scope.Handle";
        private static final String SCOPE = "codes.reason.fibre.scope.Scope";

        static List<Dependency> of(ExecutableElement executable) {
            return executable.getParameters().stream().map(Dependency::new).toList();
        }

        TypeMirror type() {
            return this.element.asType();
        }

        DeclaredType declaredType() {
            return (DeclaredType) this.type();
        }

        @Nullable String name() {
            return Annotations.name(this.type());
        }

        boolean isList() {
            return this.is(LIST);
        }

        boolean isHandle() {
            return this.is(HANDLE);
        }

        boolean isScopeHandle() {
            DeclaredType target = this.handleTarget();
            return target != null && ((TypeElement) target.asElement()).getQualifiedName().contentEquals(SCOPE);
        }

        // what a handle resolves to, which is only built once whatever takes the handle is
        @Nullable DeclaredType handleTarget() {
            if (!this.isHandle()) {
                return null;
            }
            List<? extends TypeMirror> arguments = this.declaredType().getTypeArguments();
            return !arguments.isEmpty() && arguments.getFirst() instanceof DeclaredType declared ? declared : null;
        }

        // the type and name this resolves by, which for a handle is what it refers to
        DeclaredType identityType() {
            return this.isHandle() ? Objects.requireNonNull(this.handleTarget(), "handle target") : this.declaredType();
        }

        @Nullable String identityName() {
            return this.isHandle() ? Annotations.name(this.identityType()) : this.name();
        }

        boolean isIdentityOf(TypeElement owner) {
            if (!this.is(IDENTITY) || this.name() != null || this.declaredType().getTypeArguments().size() != 1
                    || !(this.declaredType().getTypeArguments().getFirst() instanceof DeclaredType self)
                    || !self.asElement().equals(owner)) {
                return false;
            }
            List<? extends TypeParameterElement> parameters = owner.getTypeParameters();
            List<? extends TypeMirror> arguments = self.getTypeArguments();
            return arguments.size() == parameters.size() && IntStream.range(0, arguments.size())
                    .allMatch(i -> arguments.get(i) instanceof TypeVariable variable && variable.asElement().equals(parameters.get(i)));
        }

        private boolean is(String qualifiedName) {
            return this.type() instanceof DeclaredType declared
                    && ((TypeElement) declared.asElement()).getQualifiedName().contentEquals(qualifiedName);
        }

        @Nullable DeclaredType listElement() {
            List<? extends TypeMirror> arguments = this.declaredType().getTypeArguments();
            TypeMirror argument = arguments.isEmpty() ? null : arguments.getFirst();
            if (argument instanceof WildcardType wildcard) {
                argument = wildcard.getExtendsBound();
            }
            return argument instanceof DeclaredType declared ? declared : null;
        }

        DeclaredType requireListElement() {
            return Objects.requireNonNull(this.listElement(), "list element");
        }
    }

}
