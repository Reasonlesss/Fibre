package codes.reason.fibre.scope;

import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
record ScopeLifecycle(List<Runnable> enter, List<Runnable> exit) {

    ScopeLifecycle {
        enter = List.copyOf(enter);
        exit = List.copyOf(exit);
    }

}
