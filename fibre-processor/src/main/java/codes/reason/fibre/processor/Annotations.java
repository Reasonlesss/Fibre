package codes.reason.fibre.processor;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import java.util.Map;

@NullMarked
final class Annotations {

    static final String NAMED = "codes.reason.fibre.annotations.Named";
    static final String QUERYABLE = "codes.reason.fibre.annotations.Queryable";
    static final String USE = "codes.reason.fibre.annotations.Use";
    static final String ENTER = "codes.reason.fibre.annotations.Enter";
    static final String EXIT = "codes.reason.fibre.annotations.Exit";
    static final String MANAGED = "codes.reason.fibre.annotations.Managed";
    static final String SCOPED = "codes.reason.fibre.annotations.Scoped";
    static final String GAME = "codes.reason.fibre.annotations.Game";
    static final String MANAGES = "codes.reason.fibre.annotations.Manages";

    private Annotations() {
    }

    static @Nullable AnnotationMirror find(AnnotatedConstruct construct, String qn) {
        for (AnnotationMirror annotation : construct.getAnnotationMirrors()) {
            if (type(annotation).getQualifiedName().contentEquals(qn)) {
                return annotation;
            }
        }
        return null;
    }

    static boolean has(AnnotatedConstruct construct, String qn) {
        return find(construct, qn) != null;
    }

    static @Nullable AnnotationMirror findInherited(TypeElement type, String qn) {
        for (TypeElement current = type; current != null; current = superclass(current)) {
            AnnotationMirror annotation = find(current, qn);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    static boolean inherits(TypeElement type, String qn) {
        return findInherited(type, qn) != null;
    }

    static @Nullable TypeElement superclass(TypeElement type) {
        return type.getSuperclass() instanceof DeclaredType declared ? (TypeElement) declared.asElement() : null;
    }

    static @Nullable String name(AnnotatedConstruct construct) {
        AnnotationMirror named = find(construct, NAMED);
        return named != null ? (String) value(named, "value") : null;
    }

    static TypeElement type(AnnotationMirror annotation) {
        return (TypeElement) annotation.getAnnotationType().asElement();
    }

    static @Nullable Object value(AnnotationMirror annotation, String name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues().entrySet()) {
            if (entry.getKey().getSimpleName().contentEquals(name)) {
                return entry.getValue().getValue();
            }
        }
        return null;
    }

}
