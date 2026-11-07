package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

@NullMarked
final class ProcessorCollectorStep {

    private ProcessorCollectorStep() {
    }

    static ManagedClass collect(Elements elements, TypeElement type) {
        List<ExecutableElement> useConstructors = new ArrayList<>();
        ExecutableElement noArgsConstructor = null;
        List<ManagedClass.Injection> injections = new ArrayList<>();
        List<ExecutableElement> enterMethods = new ArrayList<>();
        List<ExecutableElement> exitMethods = new ArrayList<>();
        List<ManagedClass.Queried> queried = new ArrayList<>();

        for (ExecutableElement constructor : ElementFilter.constructorsIn(type.getEnclosedElements())) {
            if (constructor.getParameters().isEmpty()) {
                noArgsConstructor = constructor;
            }
            if (Annotations.has(constructor, Annotations.USE)) {
                useConstructors.add(constructor);
            }
        }
        for (Element enclosed : members(elements, type)) {
            if (Annotations.has(enclosed, Annotations.USE)) {
                injections.add(new ManagedClass.Injection(enclosed));
            }
            if (enclosed instanceof ExecutableElement method && Annotations.has(method, Annotations.ENTER)) {
                enterMethods.add(method);
            }
            if (enclosed instanceof ExecutableElement method && Annotations.has(method, Annotations.EXIT)) {
                exitMethods.add(method);
            }
            List<TypeElement> queryable = enclosed.getAnnotationMirrors().stream()
                    .map(Annotations::type)
                    .filter(annotation -> Annotations.has(annotation, Annotations.QUERYABLE))
                    .toList();
            if (!queryable.isEmpty()) {
                queried.add(new ManagedClass.Queried(enclosed, queryable));
            }
        }

        return new ManagedClass(
                type,
                kinds(type),
                List.copyOf(useConstructors),
                noArgsConstructor,
                List.copyOf(injections),
                List.copyOf(enterMethods),
                List.copyOf(exitMethods),
                List.copyOf(queried),
                options(type)
        );
    }

    private static List<Element> members(Elements elements, TypeElement type) {
        List<Element> members = new ArrayList<>();
        for (TypeElement current = type; current != null; current = Annotations.superclass(current)) {
            List<? extends Element> declared = current.getEnclosedElements().stream()
                    .filter(enclosed -> enclosed.getKind() == ElementKind.FIELD || enclosed.getKind() == ElementKind.METHOD)
                    .filter(enclosed -> !(enclosed instanceof ExecutableElement method && members.stream()
                            .anyMatch(member -> member instanceof ExecutableElement override && elements.overrides(override, method, type))))
                    .toList();
            members.addAll(0, declared);
        }
        return members;
    }

    private static List<ManagedClass.Kind> kinds(TypeElement type) {
        List<ManagedClass.Kind> kinds = new ArrayList<>();
        AnnotationMirror managed = Annotations.findInherited(type, Annotations.MANAGED);
        if (managed != null) {
            kinds.add(Boolean.TRUE.equals(Annotations.value(managed, "many")) ? ManagedClass.Kind.MANAGED_MANY : ManagedClass.Kind.MANAGED);
        }
        if (Annotations.inherits(type, Annotations.GAME)) {
            kinds.add(ManagedClass.Kind.SCOPE);
        }
        if (Annotations.inherits(type, Annotations.SCOPED)) {
            kinds.add(ManagedClass.Kind.SCOPE);
        }
        return List.copyOf(kinds);
    }

    private static @Nullable TypeElement options(TypeElement type) {
        TypeElement superclass = Annotations.superclass(type);
        return superclass != null && ManagedClass.isOptions(superclass) ? superclass : null;
    }

}
