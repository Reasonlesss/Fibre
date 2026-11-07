package codes.reason.fibre.processor;

import com.palantir.javapoet.JavaFile;
import org.jspecify.annotations.NullMarked;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NullMarked
public final class FibreProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Annotations.MANAGED, Annotations.GAME, Annotations.SCOPED);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<TypeElement> annotated = annotations.stream()
                .flatMap(annotation -> roundEnv.getElementsAnnotatedWith(annotation).stream())
                .filter(element -> element instanceof TypeElement)
                .map(element -> (TypeElement) element)
                .distinct()
                .toList();
        List<ManagedClass> collected = annotated.stream()
                .map(type -> ProcessorCollectorStep.collect(this.processingEnv.getElementUtils(), type))
                .toList();

        List<ManagedClass> valid = collected.stream()
                .filter(managed -> ProcessorValidationStep.validate(
                        this.processingEnv.getMessager(),
                        this.processingEnv.getElementUtils(),
                        this.processingEnv.getTypeUtils(),
                        managed
                ))
                .toList();
        // an abstract class is never built, only the classes that inherit its annotation
        List<ManagedClass> built = valid.stream().filter(managed -> !managed.isBase()).toList();
        List<ManagedClass> libraries = new ArrayList<>();
        Set<TypeElement> seen = collected.stream().map(ManagedClass::type).collect(Collectors.toCollection(HashSet::new));
        // library classes can depend on more library classes, so keep going until none are new
        List<ManagedClass> found = built;
        while (!found.isEmpty()) {
            found = ProcessorLibraryCollectorStep.find(this.processingEnv.getElementUtils(), found, seen).stream()
                    .map(type -> ProcessorCollectorStep.collect(this.processingEnv.getElementUtils(), type))
                    .toList();
            libraries.addAll(found);
        }
        DependencyGraph graph = new DependencyGraph(this.processingEnv.getTypeUtils(), built, libraries);
        List<ManagedClass> scopes = built.stream()
                .filter(managed -> managed.kind() == ManagedClass.Kind.SCOPE)
                .filter(root -> ProcessorCycleStep.check(this.processingEnv.getMessager(), graph, root))
                .toList();
        if (valid.size() != collected.size()) {
            // a scope could depend on any invalid class, so don't generate code that would only add more errors
            return false;
        }

        // library classes already have their _Managed class
        for (ManagedClass managed : built) {
            this.write(ProcessorManageGenerationStep.generate(this.processingEnv.getTypeUtils(), managed), managed.type());
        }
        for (ManagedClass root : scopes) {
            this.write(ProcessorScopeGenerationStep.generate(graph, root), root.type());
        }
        return false;
    }

    private void write(JavaFile file, Element element) {
        try {
            file.writeTo(this.processingEnv.getFiler());
        } catch (IOException e) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Couldn't write %s: %s".formatted(file.typeSpec().name(), e.getMessage()), element);
        }
    }

}
