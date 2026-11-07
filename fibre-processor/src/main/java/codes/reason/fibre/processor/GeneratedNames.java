package codes.reason.fibre.processor;

import com.palantir.javapoet.ClassName;
import org.jspecify.annotations.NullMarked;

import javax.lang.model.element.TypeElement;

@NullMarked
final class GeneratedNames {

    private GeneratedNames() {
    }

    static ClassName managed(TypeElement type) {
        return suffixed(type, "_Managed");
    }

    static ClassName blueprint(TypeElement type) {
        return suffixed(type, "_Blueprint");
    }

    private static ClassName suffixed(TypeElement type, String suffix) {
        ClassName name = ClassName.get(type);
        return ClassName.get(name.packageName(), String.join("_", name.simpleNames()) + suffix);
    }

}
