package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.PlayerPreloader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadInvoker implements PlayerPreloader {

    @Override
    public CompletionStage<?> preload(Game game, UUID playerId) {
        List<Method> methods =
                game.getRootScope().get(PlayerPreloadDataKeys.METHODS).orElse(List.of());
        CompletableFuture<?>[] stages = methods.stream()
                .map(method -> invoke(method, playerId))
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture<?>[]::new);
        return CompletableFuture.allOf(stages);
    }

    private static CompletionStage<?> invoke(Method method, UUID playerId) {
        try {
            Object result =
                    Objects.requireNonNull(method.invoke(null, playerId), "Player preload method returned null");
            if (result instanceof CompletionStage<?> stage) {
                return stage;
            }
            throw new IllegalStateException(
                    "Player preload method returned an invalid value: %s".formatted(method.toGenericString()));
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException(
                    "Cannot invoke player preload method %s".formatted(method.toGenericString()), exception);
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(
                    "Player preload method %s failed".formatted(method.toGenericString()), cause);
        }
    }
}
