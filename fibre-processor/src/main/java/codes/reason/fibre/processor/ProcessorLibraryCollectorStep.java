package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NullMarked
final class ProcessorLibraryCollectorStep {

    private ProcessorLibraryCollectorStep() {
    }

    static List<TypeElement> find(Elements elements, List<ManagedClass> classes, Set<TypeElement> seen) {
        List<TypeElement> libraries = new ArrayList<>();
        for (ManagedClass managed : classes) {
            managed.dependencies()
                    .filter(dependency -> !dependency.isList() && !dependency.isIdentityOf(managed.type()))
                    .forEach(dependency -> {
                        if (dependency.type() instanceof DeclaredType declared
                                && declared.asElement() instanceof TypeElement type
                                && seen.add(type) && isLibrary(elements, type)) {
                            libraries.add(type);
                        }
                    });
        }
        return libraries;
    }

    private static boolean isLibrary(Elements elements, TypeElement type) {
        if (!Annotations.inherits(type, Annotations.MANAGED)) {
            return false;
        }
        TypeElement generated = elements.getTypeElement(GeneratedNames.managed(type).canonicalName());
        AnnotationMirror manages = generated != null
                ? Annotations.find(generated, Annotations.MANAGES)
                : null;
        return manages != null && type.getQualifiedName().contentEquals((String) Annotations.value(manages, "value"));
    }

}
