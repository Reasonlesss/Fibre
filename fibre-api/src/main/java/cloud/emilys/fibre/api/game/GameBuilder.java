package cloud.emilys.fibre.api.game;

import cloud.emilys.fibre.api.scope.ObjectKey;
import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface GameBuilder {

    GameBuilder setGameType(Class<?> type);

    GameBuilder setPlugin(JavaPlugin plugin);

    GameBuilder setInputObject(ObjectKey key, Object object);

    default GameBuilder setInputObject(Type type, Object object) {
        return this.setInputObject(ObjectKey.fromType(type), object);
    }

    <T> GameBuilder addCompletionListener(Class<T> resultType, Consumer<T> consumer);

    CompletableFuture<Game> build();
}
