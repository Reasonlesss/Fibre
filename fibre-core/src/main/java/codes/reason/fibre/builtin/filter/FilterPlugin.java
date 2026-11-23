package codes.reason.fibre.builtin.filter;

import codes.reason.fibre.Fibre;
import codes.reason.fibre.plugin.Attachment;
import codes.reason.fibre.plugin.ClassAttachment;
import codes.reason.fibre.plugin.ScopePlugin;
import codes.reason.fibre.scope.Scope;
import codes.reason.fibre.scope.ScopeSetup;
import codes.reason.fibre.scope.member.BoundMethod;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class FilterPlugin implements ScopePlugin {

    private static final Attachment<ScopeFilters> FILTERS = new ClassAttachment<>(ScopeFilters.class);

    @Override
    public void setup(Fibre fibre, ScopeSetup setup) {
        List<BoundMethod> methods = setup.localMethods(Filter.class);
        if (methods.isEmpty()) {
            return;
        }
        ScopeFilters filters = setup.scope().attachment(FILTERS).orElse(null);
        if (filters == null) {
            filters = new ScopeFilters();
            setup.attach(FILTERS, filters);
        }
        for (BoundMethod method : methods) {
            filters.add(method.method().getParameterTypes()[0], value -> test(method, value, fibre.platform().logger()));
        }
    }

    // every value has to pass the scope and its parents; null values are skipped
    public static boolean allows(Scope scope, @Nullable Object... values) {
        for (Scope current = scope; current != null; current = current.parent().orElse(null)) {
            ScopeFilters filters = current.attachment(FILTERS).orElse(null);
            for (Object value : values) {
                if (value != null && (!isOwn(current, value) || filters != null && !filters.test(value))) {
                    return false;
                }
            }
        }
        return true;
    }

    // a scope holding objects of a value's type only lets its own through, e.g. a player's scope only its player
    private static boolean isOwn(Scope scope, Object value) {
        // the exact class, since a broad one like Object would match everything the scope holds
        List<?> own = scope.localList(value.getClass()).stream()
                .filter(candidate -> candidate.getClass() == value.getClass())
                .toList();
        return own.isEmpty() || own.contains(value);
    }

    // a filter that fails rejects, so a broken filter doesn't let everything through
    private static boolean test(BoundMethod method, Object value, Logger logger) {
        try {
            return Boolean.TRUE.equals(method.invoke(value));
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "%s failed, so it rejected %s".formatted(method, value), t);
            return false;
        }
    }

}
