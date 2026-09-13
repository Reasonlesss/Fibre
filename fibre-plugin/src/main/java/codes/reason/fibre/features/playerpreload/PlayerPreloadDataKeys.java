package codes.reason.fibre.features.playerpreload;

import codes.reason.fibre.api.data.RuntimeDataKey;
import codes.reason.fibre.api.scope.Scope;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadDataKeys {

    public static final RuntimeDataKey<List<Method>> METHODS =
            new RuntimeDataKey<>("fibre", "player_preload_methods", List.class);
    public static final RuntimeDataKey<Map<UUID, CompletionStage<Scope>>> SCOPES =
            new RuntimeDataKey<>("fibre", "player_preload_scopes", Map.class);

    private PlayerPreloadDataKeys() {
        throw new UnsupportedOperationException();
    }
}
