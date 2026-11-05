package codes.reason.fibre.member;

import java.util.ArrayList;
import java.util.List;

public final class FieldCheck {

    private final BoundField field;
    private final List<String> problems = new ArrayList<>();

    public FieldCheck(BoundField field) {
        this.field = field;
    }

    public FieldCheck type(Class<?> type) {
        Class<?> actual = this.field.field().getType();
        if (actual != type) {
            this.problems.add("must be %s, but is %s".formatted(
                    type.getSimpleName(),
                    actual.getSimpleName()
            ));
        }
        return this;
    }

    public FieldCheck typeSubtype(Class<?> type) {
        Class<?> actual = this.field.field().getType();
        if (!type.isAssignableFrom(actual)) {
            this.problems.add("must be %s or a subtype, but is %s".formatted(
                    type.getSimpleName(),
                    actual.getSimpleName()
            ));
        }
        return this;
    }

    public FieldCheck typeAccepts(Class<?> type) {
        Class<?> actual = this.field.field().getType();
        if (!actual.isAssignableFrom(type)) {
            this.problems.add("must accept %s, but is %s".formatted(
                    type.getSimpleName(),
                    actual.getSimpleName()
            ));
        }
        return this;
    }

    public FieldCheck writable() {
        if (!this.field.isWritable()) {
            this.problems.add("must not be final");
        }
        return this;
    }

    public BoundField check(String role) {
        if (this.problems.isEmpty()) {
            return this.field;
        }
        throw new IllegalStateException("%s is not a valid %s field:\n - %s".formatted(
            this.field, role, String.join("\n - ", this.problems)
        ));
    }

}
