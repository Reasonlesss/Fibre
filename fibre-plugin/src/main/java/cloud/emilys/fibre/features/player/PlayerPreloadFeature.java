package cloud.emilys.fibre.features.player;

import cloud.emilys.fibre.api.game.PlayerJoinToken;
import cloud.emilys.fibre.api.game.PlayerPreloader;
import cloud.emilys.fibre.core.FibreImpl;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadFeature {

    private PlayerPreloadFeature() {}

    public static void install(FibreImpl fibre) {
        Objects.requireNonNull(fibre, "fibre");
        fibre.registerPlayerJoinInitializer(token -> attachPreloads(fibre.getPlayerPreloaders(), token));
    }

    static void attachPreloads(Iterable<PlayerPreloader> preloaders, PlayerJoinToken token) {
        Objects.requireNonNull(preloaders, "preloaders");
        Objects.requireNonNull(token, "token");
        for (PlayerPreloader preloader : preloaders) {
            CompletableFuture<?> future =
                    Objects.requireNonNull(preloader.preload(token.getPlayerId()), "Player preloader returned null");
            token.waitFor(future);
        }
    }
}
