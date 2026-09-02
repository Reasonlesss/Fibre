package cloud.emilys.fibre.features.playerpreload;

import cloud.emilys.fibre.api.PrimaryThreadUtil;
import cloud.emilys.fibre.api.event.GamePlayerRemoveEvent;
import cloud.emilys.fibre.api.game.Game;
import cloud.emilys.fibre.api.game.PlayerPreloader;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.Scope;
import cloud.emilys.fibre.api.scope.creation.ScopeBlueprint;
import cloud.emilys.fibre.api.scope.creation.ScopeFactory;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class PlayerPreloadInvoker implements PlayerPreloader, Listener {

    private static final ObjectKey PLAYER_ID = ObjectKey.fromType(UUID.class);
    private static final ObjectKey PLAYER_SCOPE = ObjectKey.fromType(PlayerScopeStub.class);
    private final JavaPlugin plugin;
    private boolean listening;

    public PlayerPreloadInvoker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public CompletionStage<?> preload(Game game, UUID playerId) {
        if (!this.listening) {
            this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
            this.listening = true;
        }
        List<Method> methods =
                game.getRootScope().get(PlayerPreloadDataKeys.METHODS).orElse(List.of());
        List<Invocation> invocations = methods.stream()
                .map(method -> new Invocation(
                        PlayerPreloadUtil.getPreloadedKey(method), PlayerPreloadUtil.invoke(method, playerId)))
                .toList();
        CompletableFuture<?>[] stages = invocations.stream()
                .map(Invocation::stage)
                .map(CompletionStage::toCompletableFuture)
                .toArray(CompletableFuture<?>[]::new);
        Executor primaryExecutor = PrimaryThreadUtil.createExecutor(game.getPlugin());
        CompletionStage<Scope> preload = CompletableFuture.allOf(stages)
                .thenComposeAsync(_ -> createScope(game, playerId, invocations), primaryExecutor);
        Map<UUID, CompletionStage<Scope>> scopes =
                game.getRootScope().getOrPut(PlayerPreloadDataKeys.SCOPES, LinkedHashMap::new);
        PlayerScopeStage result = new PlayerScopeStage(scopes, playerId, primaryExecutor);
        PlayerPreloadUtil.putScope(scopes, playerId, result);
        preload.whenCompleteAsync(
                (scope, failure) -> {
                    if (failure != null) {
                        PlayerPreloadUtil.removeScope(scopes, playerId, result);
                        result.completeExceptionally(failure);
                    } else if (!result.complete(scope)) {
                        scope.close();
                    }
                },
                primaryExecutor);
        return result;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRemove(GamePlayerRemoveEvent event) {
        event.getGame()
                .getRootScope()
                .get(PlayerPreloadDataKeys.SCOPES)
                .ifPresent(scopes ->
                        PlayerPreloadUtil.closeScope(scopes, event.getPlayer().getUniqueId()));
    }

    private static CompletionStage<Scope> createScope(Game game, UUID playerId, List<Invocation> invocations) {
        Map<ObjectKey, Object> inputs = new LinkedHashMap<>();
        for (Invocation invocation : invocations) {
            if (invocation.key().equals(PLAYER_ID)) {
                throw new IllegalStateException("Player preload methods cannot provide a UUID");
            }
            Object value = invocation.stage().toCompletableFuture().join();
            if (inputs.containsKey(invocation.key())) {
                throw new IllegalStateException(
                        "Multiple player preload methods provide %s".formatted(invocation.key()));
            }
            inputs.put(invocation.key(), value);
        }

        ScopeBlueprint.Builder blueprint = ScopeBlueprint.builder()
                .setParent(game.getRootScope())
                .setInitialKey(PLAYER_SCOPE)
                .setInputObject(UUID.class, playerId)
                .setInputObjects(inputs);
        return ScopeFactory.asynchronous().initialize(blueprint.build()).thenApply(scope -> {
            scope.setName("Player " + playerId);
            scope.addFilter(UUID.class, playerId::equals);
            return scope;
        });
    }

    private record Invocation(ObjectKey key, CompletionStage<?> stage) {}

    private static final class PlayerScopeStub {}

    private static final class PlayerScopeStage extends CompletableFuture<Scope> {

        private final Map<UUID, CompletionStage<Scope>> scopes;
        private final UUID playerId;
        private final Executor primaryExecutor;

        private PlayerScopeStage(Map<UUID, CompletionStage<Scope>> scopes, UUID playerId, Executor primaryExecutor) {
            this.scopes = scopes;
            this.playerId = playerId;
            this.primaryExecutor = primaryExecutor;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            this.primaryExecutor.execute(() -> {
                PlayerPreloadUtil.removeScope(this.scopes, this.playerId, this);
                if (!cancelled) {
                    this.thenAccept(Scope::close);
                }
            });
            return cancelled;
        }
    }
}
