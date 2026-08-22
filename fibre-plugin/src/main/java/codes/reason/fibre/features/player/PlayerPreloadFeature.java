package codes.reason.fibre.features.player;

import codes.reason.fibre.api.game.PlayerJoinToken;
import codes.reason.fibre.api.game.PlayerPreloader;
import codes.reason.fibre.core.FibreImpl;
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
