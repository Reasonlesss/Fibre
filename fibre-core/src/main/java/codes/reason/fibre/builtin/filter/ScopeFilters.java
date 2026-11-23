package codes.reason.fibre.builtin.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;

// a scope's filters; each one passes any value that isn't of the type it checks
@NullMarked
final class ScopeFilters {

    private final List<Predicate<Object>> filters = new ArrayList<>();

    <T> void add(Class<T> type, Predicate<? super T> filter) {
        this.filters.add(value -> !type.isInstance(value) || filter.test(type.cast(value)));
    }

    boolean test(Object value) {
        for (Predicate<Object> filter : this.filters) {
            if (!filter.test(value)) {
                return false;
            }
        }
        return true;
    }

}
