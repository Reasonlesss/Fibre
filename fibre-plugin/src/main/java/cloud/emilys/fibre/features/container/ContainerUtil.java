package cloud.emilys.fibre.features.container;

import cloud.emilys.fibre.api.scope.Scope;
import java.util.Collection;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ContainerUtil {

    private ContainerUtil() {
        throw new UnsupportedOperationException();
    }

    public static void closeScopes(Collection<Scope> scopes) {
        List<Scope> copy = List.copyOf(scopes);
        for (int index = copy.size() - 1; index >= 0; index--) {
            try {
                copy.get(index).close();
            } catch (RuntimeException _) {
            }
        }
    }
}
