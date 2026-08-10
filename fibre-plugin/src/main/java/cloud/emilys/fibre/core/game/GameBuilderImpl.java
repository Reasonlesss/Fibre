package cloud.emilys.fibre.core.game;

import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.GameBuilder;
import cloud.emilys.fibre.api.scope.ObjectKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
final class GameBuilderImpl implements GameBuilder {

    private final GameManagerImpl manager;

    private final Map<ObjectKey, Object> objects = new HashMap<>();
    private final List<GameCompletionListener<?>> completionListeners = new ArrayList<>();

    private @Nullable Class<?> type;
    private @Nullable JavaPlugin plugin;

    GameBuilderImpl(GameManagerImpl manager) {
        this.manager = Objects.requireNonNull(manager, "manager");
    }

    @Override
    public GameBuilder setGameType(Class<?> type) {
        Objects.requireNonNull(type, "type");
        this.type = type;
        return this;
    }

    @Override
    public GameBuilder setPlugin(JavaPlugin plugin) {
        Objects.requireNonNull(plugin, "plugin");
        this.plugin = plugin;
        return this;
    }

    @Override
    public GameBuilder setInputObject(ObjectKey key, Object object) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(object, "object");
        this.objects.put(key, object);
        return this;
    }

    @Override
    public <T> GameBuilder addCompletionListener(Class<T> resultType, Consumer<T> consumer) {
        Objects.requireNonNull(resultType, "resultType");
        Objects.requireNonNull(consumer, "consumer");
        this.completionListeners.add(new GameCompletionListener<>(resultType, consumer));
        return this;
    }

    @Override
    public CompletableFuture<Game> build() {
        Objects.requireNonNull(this.type, "type");
        Objects.requireNonNull(this.plugin, "plugin");
        return GameImpl.create(this.manager, this.plugin, type, this.completionListeners, Map.copyOf(this.objects));
    }
}
