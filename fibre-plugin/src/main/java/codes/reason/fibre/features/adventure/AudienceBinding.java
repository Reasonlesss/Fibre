package codes.reason.fibre.features.adventure;

import codes.reason.fibre.api.game.Players;
import codes.reason.fibre.api.scope.ObjectKey;
import codes.reason.fibre.api.scope.ScopeResolver;
import codes.reason.fibre.api.scope.binding.Binding;
import codes.reason.fibre.api.scope.binding.BindingPriority;
import codes.reason.fibre.api.scope.binding.BindingResult;
import codes.reason.fibre.api.scope.binding.DependencySet;
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
