package cloud.emilys.fibre.core.fact;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record FactDiagnostic(Object object, String message) {

    public String format() {
        return this.formatOwner() + ": " + message;
    }

    private String formatOwner() {
        return switch (this.object) {
            case Method method ->
                "Method %s(%s)"
                        .formatted(
                                method.getName(),
                                Arrays.stream(method.getParameters())
                                        .map(Parameter::getType)
                                        .map(Class::getSimpleName)
                                        .collect(Collectors.joining(", ")));
            case Field field -> "Field %s".formatted(field.getName());
            case Class<?> aClass -> aClass.getSimpleName();
            default -> this.object.toString();
        };
    }
}
