package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.fact.FactKey;
import java.lang.reflect.Method;
import java.util.List;

public final class PlayerPreloadFacts {

    public static FactKey<List<Method>> METHODS = FactKey.of("fibre", "player_preload_methods", List.class);

    private PlayerPreloadFacts() {
        throw new UnsupportedOperationException();
    }
}
