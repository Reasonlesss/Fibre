package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class PlayerPreloadUtil {

    private PlayerPreloadUtil() {
        throw new UnsupportedOperationException();
    }

    static ObjectKey getPreloadedKey(Method method) {
        AnnotatedType returnType = method.getAnnotatedReturnType();
        if (!(returnType instanceof AnnotatedParameterizedType parameterized)
                || parameterized.getAnnotatedActualTypeArguments().length != 1) {
            throw new IllegalArgumentException(
                    "Player preload methods must declare one result type: %s".formatted(method.toGenericString()));
        }
        return ObjectKey.fromAnnotatedType(parameterized.getAnnotatedActualTypeArguments()[0]);
    }

    static CompletionStage<?> invoke(Method method, UUID playerId) {
        try {
            Object result = method.invoke(null, playerId);
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

    static void putScope(Map<UUID, CompletionStage<Scope>> scopes, UUID playerId, CompletionStage<Scope> scope) {
        if (scopes.containsKey(playerId)) {
            scopes.remove(playerId).toCompletableFuture().cancel(true);
        }
        scopes.put(playerId, scope);
    }

    static void closeScope(Map<UUID, CompletionStage<Scope>> scopes, UUID playerId) {
        if (scopes.containsKey(playerId)) {
            scopes.remove(playerId).toCompletableFuture().cancel(true);
        }
    }

    static void removeScope(Map<UUID, CompletionStage<Scope>> scopes, UUID playerId, CompletionStage<Scope> scope) {
        if (scopes.containsKey(playerId) && scopes.get(playerId) == scope) {
            scopes.remove(playerId);
        }
    }
}
