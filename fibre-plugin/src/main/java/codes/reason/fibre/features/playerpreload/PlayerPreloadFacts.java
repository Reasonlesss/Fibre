package codes.reason.fibre.features.playerpreload;

import codes.reason.fibre.api.fact.FactKey;
import java.lang.reflect.Method;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadFacts {

    public static final FactKey<List<Method>> METHODS = FactKey.of("fibre", "player_preload_methods", List.class);

    private PlayerPreloadFacts() {
        throw new UnsupportedOperationException();
    }
}
