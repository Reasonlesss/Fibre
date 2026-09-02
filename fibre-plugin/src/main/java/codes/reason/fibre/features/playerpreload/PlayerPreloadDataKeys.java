package codes.reason.fibre.features.playerpreload;

import codes.reason.fibre.api.data.RuntimeDataKey;
import java.lang.reflect.Method;
import java.util.List;

public final class PlayerPreloadDataKeys {

    public static final RuntimeDataKey<List<Method>> METHODS =
            new RuntimeDataKey<>("fibre", "player_preload_methods", List.class);

    private PlayerPreloadDataKeys() {
        throw new UnsupportedOperationException();
    }
}
