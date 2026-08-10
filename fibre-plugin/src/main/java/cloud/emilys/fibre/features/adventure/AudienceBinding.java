package cloud.emilys.fibre.features.adventure;

import cloud.emilys.fibre.api.game.Players;
import cloud.emilys.fibre.api.scope.ObjectKey;
import cloud.emilys.fibre.api.scope.ScopeResolver;
import cloud.emilys.fibre.api.scope.binding.Binding;
import cloud.emilys.fibre.api.scope.binding.BindingPriority;
import cloud.emilys.fibre.api.scope.binding.BindingResult;
import cloud.emilys.fibre.api.scope.binding.DependencySet;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class AudienceBinding implements Binding {

    private static final ObjectKey PLAYERS = ObjectKey.fromType(Players.class);
    private final PlayerAudienceMapping mapping;

    public AudienceBinding(PlayerAudienceMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public BindingPriority getPriority() {
        return BindingPriority.HIGHEST;
    }

    @Override
    public DependencySet getDependencies() {
        return DependencySet.create().add(PLAYERS);
    }

    @SuppressWarnings("resource")
    @Override
    public BindingResult make(ScopeResolver resolver) {
        Players players = (Players) resolver.get(PLAYERS).orElseThrow().getObject();
        return BindingResult.immediate(new PlayersAudience(players, this.mapping));
    }
}
