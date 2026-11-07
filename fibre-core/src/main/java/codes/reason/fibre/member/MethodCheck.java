package codes.reason.fibre.member;

import java.util.ArrayList;
import java.util.List;

public final class MethodCheck {

    private final BoundMethod method;
    private final List<String> problems = new ArrayList<>();

    public MethodCheck(BoundMethod method) {
        this.method = method;
    }

    public MethodCheck returns(Class<?> type) {
        Class<?> actual = this.method.method().getReturnType();
        if (actual != type) {
            this.problems.add("must return %s, but returns %s".formatted(type.getSimpleName(), actual.getSimpleName()));
        }
        return this;
    }

    public MethodCheck returnsSubtype(Class<?> type) {
        Class<?> actual = this.method.method().getReturnType();
        if (!type.isAssignableFrom(actual)) {
            this.problems.add("must return %s or a subtype, but returns %s"
                    .formatted(type.getSimpleName(), actual.getSimpleName()));
        }
        return this;
    }

    public MethodCheck parameterCount(int count) {
        int actual = this.method.method().getParameterCount();
        if (actual != count) {
            this.problems.add("must take %d parameter%s, but takes %d".formatted(count, count == 1 ? "" : "s", actual));
        }
        return this;
    }

    public MethodCheck parameter(int index, Class<?> type) {
        if (this.method.method().getParameterCount() <= index) {
            return this;
        }
        Class<?> actual = this.method.method().getParameterTypes()[index];
        if (actual != type) {
            this.problems.add("parameter %d must be %s, but is %s"
                    .formatted(index, type.getSimpleName(), actual.getSimpleName()));
        }
        return this;
    }

    public MethodCheck parameterAccepts(int index, Class<?> type) {
        if (this.method.method().getParameterCount() <= index) {
            return this;
        }
        Class<?> actual = this.method.method().getParameterTypes()[index];
        if (!actual.isAssignableFrom(type)) {
            this.problems.add("parameter %d must accept %s, but is %s"
                    .formatted(index, type.getSimpleName(), actual.getSimpleName()));
        }
        return this;
    }

    public MethodCheck parameterSubtype(int index, Class<?> type) {
        if (this.method.method().getParameterCount() <= index) {
            return this;
        }
        Class<?> actual = this.method.method().getParameterTypes()[index];
        if (!type.isAssignableFrom(actual)) {
            this.problems.add("parameter %d must be %s or a subtype, but is %s"
                    .formatted(index, type.getSimpleName(), actual.getSimpleName()));
        }
        return this;
    }

    public BoundMethod check(String role) {
        if (this.problems.isEmpty()) {
            return this.method;
        }
        throw new IllegalStateException("%s is not a valid %s method:\n - %s"
                .formatted(this.method, role, String.join("\n - ", this.problems)));
    }
}
