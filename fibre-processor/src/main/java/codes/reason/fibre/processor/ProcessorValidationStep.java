package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
final class ProcessorValidationStep {

    private final Messager messager;
    private final Elements elements;
    private final Types types;
    private boolean valid = true;

    private ProcessorValidationStep(Messager messager, Elements elements, Types types) {
        this.messager = messager;
        this.elements = elements;
        this.types = types;
    }

    static boolean validate(Messager messager, Elements elements, Types types, ManagedClass managed) {
        return new ProcessorValidationStep(messager, elements, types).visit(managed);
    }

    private boolean visit(ManagedClass managed) {
        TypeElement type = managed.type();
        String name = type.getSimpleName().toString();

        if (managed.kinds().size() > 1) {
            this.error(type, "%s can only be one of @Managed, @Game or @Scoped".formatted(name));
        }

        // only a superclass passes on an annotation, so interfaces and records could never share one
        if (type.getKind() != ElementKind.CLASS) {
            this.error(type, "%s must be a class".formatted(name));
            return false;
        }
        if (type.getNestingKind() == NestingKind.MEMBER && !type.getModifiers().contains(Modifier.STATIC)) {
            this.error(type, "%s must be static".formatted(name));
        } else if (type.getNestingKind() == NestingKind.LOCAL || type.getNestingKind() == NestingKind.ANONYMOUS) {
            this.error(type, "%s can't be a local class".formatted(name));
        }
        for (Element enclosing = type; enclosing instanceof TypeElement; enclosing = enclosing.getEnclosingElement()) {
            if (enclosing.getModifiers().contains(Modifier.PRIVATE)) {
                this.error(type, "%s can't be private".formatted(enclosing.getSimpleName()));
            }
        }
        if (managed.kind() == ManagedClass.Kind.SCOPE && managed.isGeneric()) {
            this.error(type, "%s can't be generic".formatted(name));
        }
        String named = managed.name();
        if (named != null && named.isBlank()) {
            this.error(type, "@Named can't be blank");
        }

        if (managed.isBase()) {
            // constructors aren't inherited, so each class that inherits the annotation picks its own
            for (ExecutableElement constructor : managed.useConstructors()) {
                this.error(constructor, "%s is abstract, so its @Use constructor is never used".formatted(name));
            }
        } else if (managed.useConstructors().size() > 1) {
            for (ExecutableElement constructor : managed.useConstructors()) {
                this.error(constructor, "%s has more than one @Use constructor".formatted(name));
            }
        } else if (managed.constructor() == null) {
            this.error(type, "%s needs a @Use constructor or a no-args constructor".formatted(name));
        }
        if (ManagedClass.isOptions(type)) {
            for (TypeMirror permitted : type.getPermittedSubclasses()) {
                // a subclass of an option would inherit @Scoped too, without being an option itself
                Element option = ((DeclaredType) permitted).asElement();
                if (!option.getModifiers().contains(Modifier.FINAL)) {
                    this.error(option, "%s must be final, since it's an option of %s".formatted(option.getSimpleName(), name));
                }
            }
        }
        managed.constructorDependencies().forEach(dependency -> this.checkDependency(managed, dependency));

        for (ManagedClass.Injection injection : managed.injections()) {
            if (!this.isOwnMember(managed, injection.member())) {
                continue;
            }
            if (injection.isField() && injection.member().getModifiers().contains(Modifier.FINAL)) {
                this.error(injection.member(), "@Use field can't be final");
            }
            injection.dependencies().forEach(dependency -> this.checkDependency(managed, dependency));
        }
        TypeElement options = managed.options();
        if (options != null) {
            managed.dependencies().forEach(dependency -> this.checkOption(managed, options, dependency));
        }

        for (ManagedClass.Queried queried : managed.queried()) {
            if (this.isOwnMember(managed, queried.member())) {
                queried.annotations().forEach(annotation -> this.checkQueried(queried.member(), annotation));
            }
        }
        this.checkUnique(managed);

        managed.enterMethods().stream().filter(method -> this.isOwnMember(managed, method)).forEach(method -> this.checkRunnable(method, "@Enter"));
        managed.exitMethods().stream().filter(method -> this.isOwnMember(managed, method)).forEach(method -> this.checkRunnable(method, "@Exit"));

        List<ManagedClass.Provider> providers = managed.providers().stream().filter(provider -> this.isOwnMember(managed, provider.method())).toList();
        providers.forEach(provider -> this.checkProvider(managed, provider));
        this.checkDuplicateProviders(providers);

        managed.usedMembers().forEach(member -> this.checkAccess(managed, member));
        return this.valid;
    }

    private void checkDependency(ManagedClass managed, ManagedClass.Dependency dependency) {
        Element element = dependency.element();
        if (dependency.isIdentityOf(managed.type())) {
            return;
        }
        if (!(dependency.type() instanceof DeclaredType)) {
            this.error(element, "%s can't be used because its type isn't a class or interface".formatted(element.getSimpleName()));
        } else if (containsTypeVariable(dependency.type())) {
            this.error(element, "%s can't be used because its type has a type variable".formatted(element.getSimpleName()));
        } else if (dependency.isList() && dependency.listElement() == null) {
            this.error(element, "%s must be a List of a class or interface".formatted(element.getSimpleName()));
        } else if (dependency.isHandle() && dependency.handleTarget() == null) {
            this.error(element, "%s must be a Handle of a class or interface".formatted(element.getSimpleName()));
        } else if (dependency.isHandle() && dependency.name() != null) {
            this.error(element, "@Named goes on what the handle refers to, e.g. Handle<@Named(\"red\") Team>");
        } else if (dependency.isHandle() && ((TypeElement) dependency.identityType().asElement()).getQualifiedName().contentEquals("java.util.List")) {
            this.error(element, "%s can't be a Handle of a List, since lists aren't bound by identity".formatted(element.getSimpleName()));
        } else if (dependency.identityName() != null && dependency.identityName().isBlank()) {
            this.error(element, "@Named can't be blank");
        }
    }

    private void checkProvider(ManagedClass managed, ManagedClass.Provider provider) {
        ExecutableElement method = provider.method();
        if (managed.kind() != ManagedClass.Kind.SCOPE) {
            this.error(method, "@Provides method must be in a @Game or @Scoped class, since only the scope object can provide");
        }
        if (!method.getModifiers().contains(Modifier.STATIC)) {
            this.error(method, "@Provides method must be static");
        }
        if (!method.getTypeParameters().isEmpty()) {
            this.error(method, "@Provides method can't be generic");
        }
        if (method.getThrownTypes().stream().anyMatch(this::isChecked)) {
            this.error(method, "@Provides method can't throw checked exceptions");
        }

        TypeMirror type = provider.type();
        if (!(type instanceof DeclaredType declared)) {
            this.error(method, "@Provides method must return a class or interface");
        } else if (containsTypeVariable(type)) {
            this.error(method, "@Provides method can't return a type with a type variable");
        } else if (Stream.of(Annotations.MANAGED, Annotations.GAME, Annotations.SCOPED).anyMatch(qn -> Annotations.inherits((TypeElement) declared.asElement(), qn))) {
            this.error(method, "@Provides method can't provide %s, since Fibre already creates it".formatted(declared.asElement().getSimpleName()));
        } else if (provider.name() != null && provider.name().isBlank()) {
            this.error(method, "@Named can't be blank");
        }

        for (ManagedClass.Dependency dependency : provider.dependencies()) {
            if (dependency.isIdentityOf(managed.type())) {
                this.error(dependency.element(), "%s can't be used, since a @Provides method has no identity".formatted(dependency.element().getSimpleName()));
            } else {
                this.checkDependency(managed, dependency);
            }
        }
    }

    private void checkDuplicateProviders(List<ManagedClass.Provider> providers) {
        for (int i = 0; i < providers.size(); i++) {
            ManagedClass.Provider provider = providers.get(i);
            boolean duplicate = providers.subList(0, i).stream().anyMatch(other -> this.types.isSameType(other.type(), provider.type())
                    && Objects.equals(other.name(), provider.name()));
            if (duplicate) {
                this.error(provider.method(), "%s is already provided by another @Provides method".formatted(provider.type()));
            }
        }
    }

    private boolean isOwnMember(ManagedClass managed, Element member) {
        TypeElement declaring = (TypeElement) member.getEnclosingElement();
        return declaring.equals(managed.type()) || Stream.of(Annotations.MANAGED, Annotations.GAME, Annotations.SCOPED).noneMatch(qn -> Annotations.inherits(declaring, qn));
    }

    private void checkOption(ManagedClass managed, TypeElement options, ManagedClass.Dependency dependency) {
        TypeMirror type = dependency.isList() && dependency.type() instanceof DeclaredType ? dependency.listElement() : dependency.type();
        if (type instanceof DeclaredType declared && !declared.asElement().equals(managed.type())
                && options.getPermittedSubclasses().stream().anyMatch(permitted -> this.types.isSameType(this.types.erasure(permitted), this.types.erasure(declared)))) {
            this.error(dependency.element(), "%s can't use %s, since only one option of %s is open at a time".formatted(
                    managed.type().getSimpleName(), declared.asElement().getSimpleName(), options.getSimpleName()));
        }
    }

    private void checkAccess(ManagedClass managed, Element member) {
        TypeElement declaring = (TypeElement) member.getEnclosingElement();
        if (member.getModifiers().contains(Modifier.PRIVATE)) {
            this.error(member, "%s can't be private".formatted(describe(member)));
        } else if (this.elements.getPackageOf(declaring).equals(this.elements.getPackageOf(managed.type()))) {
            return;
        } else if (!member.getModifiers().contains(Modifier.PUBLIC)) {
            this.error(member, "%s must be public, since %s inherits it from another package".formatted(describe(member), managed.type().getSimpleName()));
        } else if (!declaring.getModifiers().contains(Modifier.PUBLIC)) {
            this.error(member, "%s must be public, since %s inherits from it in another package".formatted(declaring.getSimpleName(), managed.type().getSimpleName()));
        }
    }

    private void checkQueried(Element member, TypeElement annotation) {
        String kind = member instanceof ExecutableElement ? Annotations.QUERYABLE_METHOD : Annotations.QUERYABLE_FIELD;
        AnnotationMirror queryable = Annotations.find(annotation, kind);
        if (queryable == null) {
            this.error(member, "@%s can't be used on a %s".formatted(annotation.getSimpleName(), member instanceof ExecutableElement ? "method" : "field"));
            return;
        }
        if (member.getModifiers().contains(Modifier.STATIC)) {
            this.error(member, "@%s %s can't be static".formatted(annotation.getSimpleName(), member instanceof ExecutableElement ? "method" : "field"));
        }
        if (!(member instanceof ExecutableElement) && member.getModifiers().contains(Modifier.FINAL)
                && Boolean.TRUE.equals(Annotations.valueWithDefaults(this.elements, queryable, "mutable"))) {
            this.error(member, "@%s field can't be final".formatted(annotation.getSimpleName()));
        }
        List<? extends AnnotationValue> allowed = this.values(queryable, "value");
        if (allowed.isEmpty()) {
            return;
        }
        if (member instanceof ExecutableElement method) {
            List<AnnotationMirror> signatures = allowed.stream().map(value -> (AnnotationMirror) value.getValue()).toList();
            if (signatures.stream().noneMatch(signature -> this.matches(method, signature))) {
                this.error(method, "@%s method must look like %s".formatted(annotation.getSimpleName(),
                        signatures.stream().map(this::describe).collect(Collectors.joining(" or "))));
            }
        } else {
            List<TypeMirror> types = allowed.stream().map(value -> (TypeMirror) value.getValue()).toList();
            if (types.stream().noneMatch(type -> this.isSubtype(member.asType(), type))) {
                this.error(member, "@%s field must be %s".formatted(annotation.getSimpleName(),
                        types.stream().map(ProcessorValidationStep::simpleName).collect(Collectors.joining(" or "))));
            }
        }
    }

    private void checkUnique(ManagedClass managed) {
        Map<TypeElement, List<Element>> byAnnotation = new LinkedHashMap<>();
        for (ManagedClass.Queried queried : managed.queried()) {
            queried.annotations().forEach(annotation -> byAnnotation.computeIfAbsent(annotation, _ -> new ArrayList<>()).add(queried.member()));
        }
        byAnnotation.forEach((annotation, members) -> {
            AnnotationMirror queryable = Stream.of(Annotations.QUERYABLE_METHOD, Annotations.QUERYABLE_FIELD)
                    .map(kind -> Annotations.find(annotation, kind))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElseThrow();
            if (members.size() > 1 && Boolean.TRUE.equals(Annotations.valueWithDefaults(this.elements, queryable, "unique"))) {
                members.stream().filter(member -> this.isOwnMember(managed, member)).forEach(member ->
                        this.error(member, "%s can only have one @%s".formatted(managed.type().getSimpleName(), annotation.getSimpleName())));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private List<? extends AnnotationValue> values(AnnotationMirror annotation, String name) {
        return (List<? extends AnnotationValue>) Annotations.valueWithDefaults(this.elements, annotation, name);
    }

    private boolean matches(ExecutableElement method, AnnotationMirror signature) {
        List<TypeMirror> parameters = this.parameters(signature);
        TypeMirror returns = this.returns(signature);
        if (method.getParameters().size() != parameters.size()) {
            return false;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (!this.isSubtype(method.getParameters().get(i).asType(), parameters.get(i))) {
                return false;
            }
        }
        return returns.getKind() == TypeKind.VOID
                ? method.getReturnType().getKind() == TypeKind.VOID
                : this.isSubtype(method.getReturnType(), returns);
    }

    private boolean isSubtype(TypeMirror actual, TypeMirror expected) {
        return actual.getKind() != TypeKind.VOID && this.types.isAssignable(this.types.erasure(actual), this.types.erasure(expected));
    }

    private List<TypeMirror> parameters(AnnotationMirror signature) {
        return this.values(signature, "parameters").stream()
                .map(value -> (TypeMirror) value.getValue())
                .toList();
    }

    private TypeMirror returns(AnnotationMirror signature) {
        return (TypeMirror) Annotations.valueWithDefaults(this.elements, signature, "returns");
    }

    private String describe(AnnotationMirror signature) {
        String parameters = this.parameters(signature).stream().map(ProcessorValidationStep::simpleName).collect(Collectors.joining(", "));
        return "%s (%s)".formatted(simpleName(this.returns(signature)), parameters);
    }

    private static String simpleName(TypeMirror type) {
        return type instanceof DeclaredType declared ? declared.asElement().getSimpleName().toString() : type.toString();
    }

    private void checkRunnable(ExecutableElement method, String annotation) {
        if (method.getModifiers().contains(Modifier.STATIC)) {
            this.error(method, "%s method can't be static".formatted(annotation));
        }
        if (!method.getParameters().isEmpty()) {
            this.error(method, "%s method can't have parameters".formatted(annotation));
        }
        if (method.getThrownTypes().stream().anyMatch(this::isChecked)) {
            this.error(method, "%s method can't throw checked exceptions".formatted(annotation));
        }
    }

    private boolean isChecked(TypeMirror thrown) {
        return !this.types.isAssignable(thrown, this.elements.getTypeElement("java.lang.RuntimeException").asType())
                && !this.types.isAssignable(thrown, this.elements.getTypeElement("java.lang.Error").asType());
    }

    private static boolean containsTypeVariable(@Nullable TypeMirror type) {
        return switch (type) {
            case TypeVariable ignored -> true;
            case DeclaredType declared -> declared.getTypeArguments().stream().anyMatch(ProcessorValidationStep::containsTypeVariable);
            case WildcardType wildcard -> containsTypeVariable(wildcard.getExtendsBound()) || containsTypeVariable(wildcard.getSuperBound());
            case ArrayType array -> containsTypeVariable(array.getComponentType());
            case null, default -> false;
        };
    }

    private static String describe(Element member) {
        return member.getKind() == ElementKind.CONSTRUCTOR
                ? "Constructor of %s".formatted(member.getEnclosingElement().getSimpleName())
                : member.getSimpleName().toString();
    }

    private void error(Element element, String message) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, message, element);
        this.valid = false;
    }

}
