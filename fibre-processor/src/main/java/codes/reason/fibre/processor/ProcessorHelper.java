package codes.reason.fibre.processor;

import com.palantir.javapoet.ClassName;
import org.jspecify.annotations.NullMarked;

import javax.lang.model.element.TypeElement;

@NullMarked
final class ProcessorHelper {

    static final ClassName SCOPE_BUILDER = ClassName.get("codes.reason.fibre.scope", "ScopeBuilder");

    // members of the generated _Managed classes
    static final String CREATE = "create";
    static final String IDENTITY_FIELD = "IDENTITY";

    private ProcessorHelper() {
    }

    static ClassName managedName(TypeElement type) {
        return suffixed(type, "_Managed");
    }

    static ClassName blueprintName(TypeElement type) {
        return suffixed(type, "_Blueprint");
    }

    static String decapitalize(String name) {
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }

    private static ClassName suffixed(TypeElement type, String suffix) {
        ClassName name = ClassName.get(type);
        return ClassName.get(name.packageName(), String.join("_", name.simpleNames()) + suffix);
    }

}
